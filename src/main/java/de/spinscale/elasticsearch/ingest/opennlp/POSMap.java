package de.spinscale.elasticsearch.ingest.opennlp;

import java.util.HashMap;
import java.util.Map;

// Returns the description of POS tags using a LUT
public class POSMap {

   private Map<String, String> table;

    public POSMap(){
        table = new HashMap<>();
        table.put("C", "coordinating conjunction");
        table.put("D", "cardinal number");
        table.put("T", "determiner");
        table.put("X", "existential there");
        table.put("W", "foreign word");
        table.put("N", "preposition or subordinating conjunction");
        table.put("J", "adjective");
        table.put("JR", "adjective, comparative");
        table.put("JS", "adjective, superlative");
        table.put("LS", "list item marker");
        table.put("MD", "modal");
        table.put("NN", "noun, singular or mass");
        table.put("NNS", "noun, plural");
        table.put("NNP", "proper noun, singular");
        table.put("NNPS", "proper noun, plural");
        table.put("PDT", "predeterminer");
        table.put("POS", "possessive ending");
        table.put("PRP", "personal pronoun");
        table.put("PRP$", "possessive pronoun");
        table.put("RB", "adverb");
        table.put("RBR", "adverb, comparative");
        table.put("RBS", "adverb, superlative");
        table.put("RP", "particle");
        table.put("SYM", "symbol");
//        table.put("TO", "to");
        table.put("UH", "interjection");
        table.put("VB", "verb, base form");
        table.put("VBD", "verb, past tense");
        table.put("VBG", "verb, gerund or present participle");
        table.put("VBN", "verb, past participle");
        table.put("VBP", "verb, non-3rd person singular present");
        table.put("VBZ", "verb, 3rd person singular present");
        table.put("WDT", "wh-determiner");
        table.put("WP", "wh-pronoun");
        table.put("WPS", "possessive wh-pronoun");
        table.put("WRB", "wh-adverb");
    }

    public String lookup(String x){

        if(table.containsKey(x)){
            return table.get(x);
        }

        return null;
    }
}
