package com.dreamcloud.esa.documentPreprocessor;

import java.util.ArrayList;

public class ChainedPreprocessor implements DocumentPreprocessor {
    ArrayList<DocumentPreprocessor> preprocessors;
    public ChainedPreprocessor(ArrayList<DocumentPreprocessor> preprocessors) {
        this.preprocessors = preprocessors;
    }

    public String process(String document) throws Exception {
        for (DocumentPreprocessor preprocessor: preprocessors) {
            document = preprocessor.process(document);
        }
        return document;
    }
}