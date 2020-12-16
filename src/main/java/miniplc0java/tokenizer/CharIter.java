package miniplc0java.tokenizer;

import miniplc0java.util.MyIterator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Optional;

public class CharIter extends MyIterator<Character> {
    BufferedReader br;
    public CharIter(Reader reader) {
        this.br = new BufferedReader(reader);
    }


    @Override
    protected Optional<Character> getNext() throws IOException {
        int next = br.read();
        if(next == -1)
            return Optional.empty();
        else
            return Optional.of((char)next);
    }
}