package be.vanoosten.esa;

import java.io.File;
import java.util.Iterator;

import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;

import static org.apache.lucene.util.Version.LUCENE_48;

/**
 *
 * @author Philip van Oosten
 */
public class EnwikiFactory extends WikiFactory {
    private static String getWikiDumpFile(int year) {
        if (year == 2021) {
            return "enwiki-20210801-pages-articles-multistream.xml.bz2";
        } else if(year == 20211) {
            return "simplewiki-20210101-pages-articles-multistream.xml.bz2";
        } else {
            return "enwiki-20080103-pages-articles.xml.bz2";
        }
    }

    public static CharArraySet getExtendedStopWords() {
        CharArraySet basic = StopAnalyzer.ENGLISH_STOP_WORDS_SET;
        CharArraySet stopwords = new CharArraySet(LUCENE_48, 256, true);
        for (Iterator<Object> it = basic.iterator(); it.hasNext(); ) {
            Object stopword = it.next();
            stopwords.add(stopword);
        }
        String[] extended = {
            //Nouns
            "anything",
            "something",
            "nothing",
            "everything",
            "anywhere",
            "somewhere",
            "nowhere",
            "piece",
            "i",
            "me",
            "my",
            "myself",
            "we",
            "our",
            "ours",
            "ourselves",
            "you",
            "your",
            "yours",
            "yourself",
            "yourselves",
            "he",
            "him",
            "his",
            "himself",
            "she",
            "her",
            "hers",
            "herself",
            "it",
            "its",
            "itself",
            "they",
            "them",
            "their",
            "theirs",
            "themselves",
            "us",
            "one",
            "anyone",
            "noone",
            "someone",
            "everyone",
            "anybody",
            "everybody",
            "somebody",
            "person",
            "nobody",
            "what",
            "which",
            "who",
            "whom",
            "this",
            "that",
            "these",
            "those",
            "a",
            "an",
            "the",
            "and",
            "but",
            "if",
            "or",
            "because",
            "as",
            "until",
            "while",
            "of",
            "at",
            "by",
            "for",
            "with",
            "without",
            "about",
            "against",
            "between",
            "into",
            "through",
            "during",
            "before",
            "after",
            "above",
            "below",
            "to",
            "from",
            "up",
            "down",
            "in",
            "out",
            "on",
            "off",
            "over",
            "under",
            "again",
            "further",
            "then",
            "once",
            "here",
            "there",
            "when",
            "where",
            "why",
            "how",
            "all",
            "any",
            "both",
            "each",
            "few",
            "more",
            "most",
            "other",
            "some",
            "such",
            "only",
            "own",
            "same",
            "so",
            "than",
            "too",
            "very",
            "can",
            "will",
            "just",
            "don",
            "should",
            "now",
            "unless",
            "though",

            //Normal verbs
            "am",
            "are",
            "be",
            "been",
            "being",
            "bring",
            "can",
            "come",
            "could",
            "did",
            "do",
            "does",
            "doing",
            "don", //todo: for "don"t", find better solution
            "get",
            "go",
            "had",
            "has",
            "have",
            "having",
            "is",
            "just",
            "know",
            "look",
            "looking",
            "may",
            "say",
            "see",
            "seem",
            "should",
            "tell",
            "telling",
            "use",
            "want",
            "was",
            "were",
            "will",
            "would",
            "get",
            "began",
            "begin",
            "start",
            "think",
            "felt",
            "feel",
            "know",
            "knew",
            "want",
            "notic",
            "got",
            "tri",
            //Adverbs and adjectives
            "able",
            "also",
            "atop",
            "away",
            "different",
            "every",
            "like",
            "neither",
            "no",
            "none",
            "not",
            "onto",
            "really",
            "same",
            "similar",
            "some",
            "sort",
            "sure",
            "toward",
            "whether",
            "not",
            "n't",

            //Werid things
            "s",
            "t",
            "'",
          "'m",
          "'re",
          "'s",
          "m",
          "re",
          "/",
          "\\",
          "'d",
        };
        for (String stop: extended) {
            stopwords.add(stop);
        }
        return stopwords;
    }

    public EnwikiFactory() {
        super(indexRootPath(),
                new File(indexRootPath(), String.join(File.separator, getWikiDumpFile(20211))),
                getExtendedStopWords());
    }

    private static File indexRootPath() {
        return new File(String.join(File.separator, "F:", "dev", "esa", "enwiki"));
    }
}
