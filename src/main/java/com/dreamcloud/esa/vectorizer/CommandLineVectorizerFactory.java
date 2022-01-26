package com.dreamcloud.esa.vectorizer;

import com.dreamcloud.esa.EsaOptions;
import com.dreamcloud.esa_score.analysis.BM25Calculator;
import com.dreamcloud.esa_score.analysis.TfIdfAnalyzer;
import com.dreamcloud.esa_score.analysis.TfIdfCalculator;

public class CommandLineVectorizerFactory implements VectorizerFactory {
    EsaOptions options;

    public CommandLineVectorizerFactory(EsaOptions options) {
        this.options = options;
    }

    @Override
    public TextVectorizer getVectorizer() {
        return new SqlVectorizer(new VectorBuilder(options.sourceOptions.scoreReader, options.sourceOptions.collectionInfo, new TfIdfAnalyzer(new BM25Calculator(new TfIdfCalculator(options.tfIdfQueryMode)), options.analyzer, options.sourceOptions.collectionInfo), options.preprocessor, options.pruneOptions));
    }
}
