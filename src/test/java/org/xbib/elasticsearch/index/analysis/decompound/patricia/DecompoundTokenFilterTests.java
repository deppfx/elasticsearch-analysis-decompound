package org.xbib.elasticsearch.index.analysis.decompound.patricia;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.analysis.common.CommonAnalysisPlugin;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.ESTokenStreamTestCase;
import org.xbib.elasticsearch.plugin.analysis.decompound.AnalysisDecompoundPlugin;

import java.io.StringReader;

/**
  Decompound token filter tests.
 */
public class DecompoundTokenFilterTests extends ESTokenStreamTestCase {

    public void test() throws Exception {
        String source = "Die Jahresfeier der Rechtsanwaltskanzleien auf dem Donaudampfschiff hat viel Ökosteuer gekostet";
        String[] expected = {
            "Die",
            "Die",
            "Jahresfeier",
            "Jahr",
            "feier",
            "der",
            "der",
            "Rechtsanwaltskanzleien",
            "Recht",
            "anwalt",
            "kanzlei",
            "auf",
            "auf",
            "dem",
            "dem",
            "Donaudampfschiff",
            "Donau",
            "dampf",
            "schiff",
            "hat",
            "hat",
            "viel",
            "viel",
            "Ökosteuer",
            "Ökosteuer",
            "gekostet",
            "gekosten"
        };
        String resource = "/org/xbib/elasticsearch/index/analysis/decompound/patricia/decompound_analysis.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        ESTestCase.TestAnalysis analysis = ESTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new AnalysisDecompoundPlugin(Settings.EMPTY), new CommonAnalysisPlugin());
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("decomp");
        Tokenizer tokenizer = analysis.tokenizer.get("standard").create();
        tokenizer.setReader(new StringReader(source));
        assertTokenStreamContents(tokenFilter.create(tokenizer), expected);
    }

    public void testWithSubwordsOnlyAndKeywords() throws Exception {
        String source = "Das ist ein Schlüsselwort, ein Bindestrichwort";
        String[] expected = {
                "Da",
                "ist",
                "ein",
                "Schlüssel",
                "wort",
                "ein",
                "Bindestrich",
                "wort"
        };
        String resource = "/org/xbib/elasticsearch/index/analysis/decompound/patricia/keywords_analysis.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        ESTestCase.TestAnalysis analysis = ESTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new AnalysisDecompoundPlugin(Settings.EMPTY), new CommonAnalysisPlugin());
        Analyzer analyzer = analysis.indexAnalyzers.get("with_subwords_only");
        assertNotNull(analyzer);
        assertTokenStreamContents(analyzer.tokenStream("test-field", source), expected);

        String[] expected_keywords = {
                "Das",
                "Da",
                "ist",
                "ist",
                "ein",
                "ein",
                "Schlüsselwort",
                "ein",
                "ein",
                "Bindestrichwort",
                "Bindestrich",
                "wort"
        };
        analyzer = analysis.indexAnalyzers.get("with_keywords");
        assertNotNull(analyzer);
        assertTokenStreamContents(analyzer.tokenStream("test-field", source), expected_keywords);
    }

    public void testSynonym() throws Exception {
        String source = "Die deutsche Spielbankgesellschaft ist nicht die Deutsche Bank";
        String[] expected = {
                "die",
                "deutsche",
                "spielbankgesellschaft",
                "spiel",
                "bank",
                "gesellschaft",
                "ist",
                "nicht",
                "deutschebank"
        };
        String resource = "/org/xbib/elasticsearch/index/analysis/decompound/patricia/synonym_analysis.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        ESTestCase.TestAnalysis analysis = ESTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new AnalysisDecompoundPlugin(Settings.EMPTY), new CommonAnalysisPlugin());
        Analyzer analyzer = analysis.indexAnalyzers.get("decompounding_with_synonym");
        assertNotNull(analyzer);
        assertTokenStreamContents(analyzer.tokenStream("test-field", source), expected);
    }

}
