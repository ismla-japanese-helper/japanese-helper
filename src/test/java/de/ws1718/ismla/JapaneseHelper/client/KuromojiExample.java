package de.ws1718.ismla.JapaneseHelper.client;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;
import com.google.common.base.Joiner;
import com.mariten.kanatools.KanaConverter;

import java.util.ArrayList;
import java.util.List;

public class KuromojiExample {
    public static void main(String[] args) {
        String hiragana = "は";
        String katakana = "ハ";
        int conv_op_flags = KanaConverter.OP_ZEN_KATA_TO_ZEN_HIRA;
        // Note that this is recorded in Katakana, while Wiktionary uses Hiragana. Seems some conversion will be needed.
        String kata = KanaConverter.convertKana(katakana, conv_op_flags);
        // System.out.println(hiragana.equals(kata));

        Tokenizer tokenizer = new Tokenizer() ;
        // List<Token> tokens = tokenizer.tokenize("お寿司が食べたい。");
        // List<Token> tokens = tokenizer.tokenize("この動物は日本語で何と言いますか。");
        // List<Token> tokens = tokenizer.tokenize("それは本当に熱かった");
        // List<Token> tokens = tokenizer.tokenize("ありません");
        List<Token> tokens = tokenizer.tokenize("計算言語学");

        ArrayList<String> l = new ArrayList<>();
        l.add("asdf");
        l.add("asdgsdfg");
        // l.remove(1);

        Joiner joiner = Joiner.on("");

        System.out.println(joiner.join(l));

        for (Token token : tokens) {
            // System.out.println(token.getSurface() + "\t" + token.getAllFeatures());
            System.out.println(token.getSurface());
            System.out.println(token.getBaseForm());
            System.out.println(token.getConjugationType());
            System.out.println(token.getConjugationForm());
        }
    }
}