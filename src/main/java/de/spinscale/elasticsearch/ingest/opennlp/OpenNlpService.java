/*
 * Copyright [2016] [Alexander Reelsen]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package de.spinscale.elasticsearch.ingest.opennlp;


import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.util.Span;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.util.Supplier;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.StopWatch;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.set.Sets;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OpenNLP name finders are not thread safe, so we load them via a thread local hack
 */
public class OpenNlpService {

    private static final Logger logger = LogManager.getLogger(OpenNlpService.class);
    private final Path configDirectory;
    private Settings settings;

    private ThreadLocal<TokenNameFinderModel> threadLocal = new ThreadLocal<>();
    private Map<String, TokenNameFinderModel> nameFinderModels = new ConcurrentHashMap<>();
    private POSModel posModel;

    OpenNlpService(Path configDirectory, Settings settings) {
        this.configDirectory = configDirectory;
        this.settings = settings;
    }

    public Set<String> getModels() {
        return IngestOpenNlpPlugin.MODEL_FILE_SETTINGS.getAsMap(settings).keySet();
    }

    protected OpenNlpService start() {

        StopWatch sw = new StopWatch("models-loading");
        Map<String, String> settingsMap = IngestOpenNlpPlugin.MODEL_FILE_SETTINGS.getAsMap(settings);

        for (Map.Entry<String, String> entry : settingsMap.entrySet()) {

            String name = entry.getKey();
            sw.start(name);

            Path path = configDirectory.resolve(entry.getValue());
            String filename = path.getFileName().toString();


            try (InputStream is = Files.newInputStream(path)) {

                if(filename.contains("ner")){
                    nameFinderModels.put(name, new TokenNameFinderModel(is));
                }

                if(filename.contains("pos")){
                    posModel = new POSModel(is);
                }

            } catch (IOException e) {
                logger.error((Supplier<?>) () -> new ParameterizedMessage(
                        "Could not load model [{}] with path [{}]", name, path), e);
            }
            sw.stop();
        }

        if (settingsMap.keySet().size() == 0) {
            logger.error("Did not load any models for ingest-opennlp plugin, none configured");
        } else {
            logger.info("Read models in [{}] for {}", sw.totalTime(), settingsMap.keySet());
        }

        return this;
    }

    public Set<String> findEntities(String content, String field) {

        try {

            TokenNameFinderModel finderModel = nameFinderModels.get(field);

            if (threadLocal.get() == null || !threadLocal.get().equals(finderModel)) {
                threadLocal.set(finderModel);
            }

            String[] tokens = SimpleTokenizer.INSTANCE.tokenize(content);
            Span spans[] = new NameFinderME(finderModel).find(tokens);
            String[] names = Span.spansToStrings(spans, tokens);
            return Sets.newHashSet(names);

        } finally {
            threadLocal.remove();
        }
    }

    public Map<String, Map<String, Integer>> tagPOS(String content){

        try{

           if(posModel == null){
               throw new ElasticsearchException("Cannot tag POS because " +
                       "POS model is missing");
           }

           POSTaggerME tagger = new POSTaggerME(posModel);
           Map <String, Map<String, Integer>> m = new HashMap<>();

            // Avoided the whitespace tokenizer here because it
            // tokenizes words with the punctuation in them
            // ie: My name is Alex.
            // will have (Alex.) as a token
           String[] tokens = SimpleTokenizer.INSTANCE.tokenize(content);
           String[] tags = tagger.tag(tokens);

           for(int i = 0; i < tags.length; i++){

               String tag = tags[i].trim();
               String word = tokens[i].trim();

               if(word.replaceAll("\\p{P}", "").length() <= 1) continue;

               m.putIfAbsent(tag, new HashMap<>());
               m.get(tag).putIfAbsent(word, 0);
               Integer oldValue = m.get(tag).get(word);
               m.get(tag).put(word, oldValue + 1);
           }
       return m;

       }
       catch(Exception e){
            logger.error(e.getMessage());
       }

        return null;
    }
}
