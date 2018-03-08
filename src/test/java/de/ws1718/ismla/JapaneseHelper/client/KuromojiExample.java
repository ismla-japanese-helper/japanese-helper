package de.ws1718.ismla.JapaneseHelper.client;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;
import com.mariten.kanatools.KanaConverter;

import java.util.List;

public class KuromojiExample {
    public static void main(String[] args) {
        String hiragana = "は";
        String katakana = "ハ";
        int conv_op_flags = KanaConverter.OP_ZEN_KATA_TO_ZEN_HIRA;
        // Note that this is recorded in Katakana, while Wiktionary uses Hiragana. Seems some conversion will be needed.
        String kata = KanaConverter.convertKana(katakana, conv_op_flags);
        System.out.println(hiragana.equals(kata));

        Tokenizer tokenizer = new Tokenizer() ;
        List<Token> tokens = tokenizer.tokenize("お寿司が食べたい。");
        for (Token token : tokens) {
            System.out.println(token.getSurface() + "\t" + token.getAllFeatures());
        }
    }
}