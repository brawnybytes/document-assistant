package com.brawnybytes.docassistant.extraction;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class TextExtractionService {

    // Extracts text one page at a time instead of the whole document as one
    // blob. This is the only reason we can cite a page number later when the
    // LLM answers a question - if we pulled it all as one string we'd lose
    // track of which text came from which page.
    public List<ExtractedPage> extractPages(byte[] pdfBytes) throws IOException {
        List<ExtractedPage> pages = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            int totalPages = document.getNumberOfPages();

            for (int pageNumber = 1; pageNumber <= totalPages; pageNumber++) {
                stripper.setStartPage(pageNumber);
                stripper.setEndPage(pageNumber);
                String text = stripper.getText(document).trim();
                pages.add(new ExtractedPage(pageNumber, text));
            }
        }

        return pages;
    }
}