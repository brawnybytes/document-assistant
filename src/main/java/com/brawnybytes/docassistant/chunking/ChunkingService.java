package com.brawnybytes.docassistant.chunking;

import com.brawnybytes.docassistant.extraction.ExtractedPage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkingService {

    private static final int CHUNK_SIZE_CHARS = 1000;
    private static final int OVERLAP_CHARS = 150;

    // Chunks every page's text separately (never across a page boundary) so
    // every chunk belongs to exactly one page number - that's what lets us
    // cite a single page later. chunkIndex just keeps counting up across
    // the whole document so each chunk has a stable position.
    public List<Chunk> chunkDocument(List<ExtractedPage> pages) {
        List<Chunk> allChunks = new ArrayList<>();
        int chunkIndex = 0;

        for (ExtractedPage page : pages) {
            for (String piece : chunkText(page.text())) {
                allChunks.add(new Chunk(page.pageNumber(), chunkIndex, piece));
                chunkIndex++;
            }
        }

        return allChunks;
    }

    // Builds chunks word by word (never splits a word in half) up to roughly
    // CHUNK_SIZE_CHARS. When a chunk is finalized, the next one starts by
    // carrying over the last OVERLAP_CHARS worth of words from the previous
    // chunk, so context isn't lost right at the cut point.
    private List<String> chunkText(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }

        String[] words = text.split("\\s+");
        List<String> currentChunkWords = new ArrayList<>();
        int currentLength = 0;

        for (String word : words) {
            int wordLength = word.length() + 1;

            if (currentLength + wordLength > CHUNK_SIZE_CHARS && !currentChunkWords.isEmpty()) {
                chunks.add(String.join(" ", currentChunkWords));

                List<String> overlapWords = takeTrailingWords(currentChunkWords, OVERLAP_CHARS);
                currentChunkWords = new ArrayList<>(overlapWords);
                currentLength = totalLength(currentChunkWords);
            }

            currentChunkWords.add(word);
            currentLength += wordLength;
        }

        if (!currentChunkWords.isEmpty()) {
            chunks.add(String.join(" ", currentChunkWords));
        }

        return chunks;
    }

    private List<String> takeTrailingWords(List<String> words, int maxChars) {
        List<String> result = new ArrayList<>();
        int length = 0;
        for (int i = words.size() - 1; i >= 0; i--) {
            String word = words.get(i);
            length += word.length() + 1;
            if (length > maxChars) {
                break;
            }
            result.add(0, word);
        }
        return result;
    }

    private int totalLength(List<String> words) {
        return words.stream().mapToInt(w -> w.length() + 1).sum();
    }
}