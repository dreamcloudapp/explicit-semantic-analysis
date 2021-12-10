package com.dreamcloud.esa.indexer;

import com.dreamcloud.esa.database.InverseTermMap;
import com.dreamcloud.esa.database.TermScore;
import com.dreamcloud.esa.database.TermScores;
import com.dreamcloud.esa.similarity.TrueTFIDFSimilarity;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class IndexPruner {
    protected int windowSize;
    protected double maximumDrop;
    protected AtomicInteger processedTerms = new AtomicInteger(0);
    final Set<String> termSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
    protected int totalTerms = 0;

    public IndexPruner(int windowSize, double maximumDrop) {
        this.windowSize = windowSize;
        this.maximumDrop = maximumDrop;
    }

    public IndexPruner() {
        this(100, 0.05);
    }

    public void prune(Path indexPath, Path prunedPath) throws IOException {
        Directory termDocDirectory = FSDirectory.open(indexPath);
        IndexReader termDocReader = DirectoryReader.open(termDocDirectory);
        IndexSearcher docSearcher = new IndexSearcher(termDocReader);
        //docSearcher.setSimilarity(new TrueTFIDFSimilarity());
        ExecutorService executorService = Executors.newFixedThreadPool(termDocReader.leaves().size());
        ArrayList<Callable<Integer>> processors = new ArrayList<>();

        for(int l = 0; l < termDocReader.leaves().size(); l++) {
            TermsEnum terms = termDocReader.leaves().get(l).reader().terms("text").iterator();
            for (BytesRef bytesRef = terms.term(); terms.next() != null; ) {
                if (bytesRef.length > 32) {
                    //todo: move to length limit filter
                    continue;
                }
                String term = bytesRef.utf8ToString();
                if (!termSet.contains(term)) {
                    termSet.add(term);
                    totalTerms++;
                }
            }
        }
        termSet.clear();

        System.out.println("Total terms: " + totalTerms);

        for (int i=0; i<termDocReader.leaves().size(); i++) {
            int finalI = i;
            TermsEnum terms = termDocReader.leaves().get(finalI).reader().terms("text").iterator();
            processors.add(() -> this.pruneTerms(terms, docSearcher, finalI));
        }

        try{
            List<Future<Integer>> futures = executorService.invokeAll(processors);
            for(Future<Integer> future: futures){
                if (future.isDone()) {
                    System.out.println("Thread completed.");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(24 , TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Integer pruneTerms(TermsEnum terms, IndexSearcher docSearcher, int threadNum) throws IOException {
        try {
            InverseTermMap termMap = new InverseTermMap();
            for (BytesRef bytesRef = terms.term(); terms.next() != null; ) {
                if (bytesRef.length > 32) {
                    //todo: move to length limit filter
                    continue;
                }

                String term = bytesRef.utf8ToString();
                if (!term.equals("link") && !term.equals("number")) {
                   continue;
                }

                synchronized (termSet) {
                    if (!termSet.contains(term)) {
                        termSet.add(term);
                    } else {
                        continue;
                    }
                }

                TermScores scores = new TermScores(bytesRef);
                TopDocs td = SearchTerm(scores.term, docSearcher);
                if (td.scoreDocs.length == 0) {
                    continue;
                }

                float topScore = td.scoreDocs[0].score;
                float firstScore = 0;
                int w = 0;
                for (ScoreDoc scoreDoc: td.scoreDocs) {
                    scores.scores.add(new TermScore(scoreDoc.doc, scoreDoc.score));
                    if (w == 0) {
                        firstScore = scoreDoc.score;
                    }
                    if (w == windowSize - 1) {
                        float lastScore = scoreDoc.score;
                        if ((firstScore - lastScore) < (topScore * this.maximumDrop)) {
                            break;
                        } else {
                            w = -1;
                        }
                    }
                    w++;
                }
                //termMap.saveTermScores(scores);
                if (term.equals("link") || term.equals("number")) {
                    System.out.println("Term '" + term + "' has " + td.scoreDocs.length + " concepts pruned to " + scores.scores.size());
                }

                int termCount = processedTerms.getAndIncrement();
                if (termCount % 1000 == 0) {
                    System.out.println("processed term" + "\t[" + termCount + " / " + totalTerms + "] (" + term + ")");
                }
            }
            return 0;
        } catch (Exception e) {
            System.out.println("Error in thread " + threadNum + ":");
            e.printStackTrace();
            return -1;
        }
    }

    private TopDocs SearchTerm(BytesRef bytesRef, IndexSearcher docSearcher) throws IOException {
        TopScoreDocCollector collector = TopScoreDocCollector.create(docSearcher.getIndexReader().numDocs(), docSearcher.getIndexReader().numDocs());
        Term term = new Term("text", bytesRef);
        Query query = new TermQuery(term);
        docSearcher.search(query, collector);
        return collector.topDocs();
    }
}
