/* Generated By:JavaCC: Do not edit this line. OWLCompactParserConstants.java */
package edu.stanford.smi.protegex.owl.compactparser;

public interface OWLCompactParserConstants {

    int EOF = 0;

    int SINGLE_LINE_COMMENT = 9;

    int FORMAL_COMMENT = 10;

    int MULTI_LINE_COMMENT = 11;

    int INTEGER_LITERAL = 13;

    int FLOATING_POINT_LITERAL = 14;

    int EXPONENT = 15;

    int STRING_LITERAL = 16;

    int XSD = 17;

    int ONE_OF = 18;

    int IDENTIFIER = 19;

    int LETTER = 20;

    int DIGIT = 21;

    int DEFAULT = 0;

    int IN_SINGLE_LINE_COMMENT = 1;

    int IN_FORMAL_COMMENT = 2;

    int IN_MULTI_LINE_COMMENT = 3;

    String[] tokenImage = {
            "<EOF>",
            "\" \"",
            "\"\\t\"",
            "\"\\n\"",
            "\"\\r\"",
            "\"\\f\"",
            "\"//\"",
            "<token of kind 7>",
            "\"/*\"",
            "<SINGLE_LINE_COMMENT>",
            "\"*/\"",
            "\"*/\"",
            "<token of kind 12>",
            "<INTEGER_LITERAL>",
            "<FLOATING_POINT_LITERAL>",
            "<EXPONENT>",
            "<STRING_LITERAL>",
            "<XSD>",
            "\"owl:oneOf{\"",
            "<IDENTIFIER>",
            "<LETTER>",
            "<DIGIT>",
            "\"|\"",
            "\"&\"",
            "\"!\"",
            "\"(\"",
            "\")\"",
            "\"<\"",
            "\">\"",
            "\"=\"",
            "\"$\"",
            "\"{\"",
            "\"}\"",
            "\"*\"",
            "\"?\"",
    };

}