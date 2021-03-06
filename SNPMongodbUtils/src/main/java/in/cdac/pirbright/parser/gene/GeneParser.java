/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package in.cdac.pirbright.parser.gene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sandeep
 */
public class GeneParser {

    private static String HEADER_MARKER = "#";
    private static int RECORD_COUNT = 5000;
    private GeneLoaderMongoDB mongoDBGeneLoader;
    private File geneFile;
    private Map<String, String> chromosomeMap;

    GeneParser(File geneFile, GeneLoaderMongoDB mongoDBGeneLoader) {
        this.geneFile = geneFile;
        this.mongoDBGeneLoader = mongoDBGeneLoader;
        chromosomeMap = new HashMap<>();

        chromosomeMap.put("NC_006088.5", "CM000093.5");
        chromosomeMap.put("NC_006088.6", "CM000094.5");
        chromosomeMap.put("NC_006088.7", "CM000095.5");
        chromosomeMap.put("NC_006091.5", "CM000096.5");
        chromosomeMap.put("NC_006092.5", "CM000097.5");
        chromosomeMap.put("NC_006093.5", "CM000098.5");
        chromosomeMap.put("NC_006094.5", "CM000099.5");
        chromosomeMap.put("NC_006095.5", "CM000100.5");
        chromosomeMap.put("NC_006096.5", "CM000101.5");
        chromosomeMap.put("NC_006097.5", "CM000102.5");
        chromosomeMap.put("NC_006098.5", "CM000103.5");
        chromosomeMap.put("NC_006099.5", "CM000104.5");
        chromosomeMap.put("NC_006100.5", "CM000105.5");
        chromosomeMap.put("NC_006101.5", "CM000106.5");
        chromosomeMap.put("NC_006102.5", "CM000107.5");
        chromosomeMap.put("NC_006103.5", "CM000108.5");
        chromosomeMap.put("NC_006104.5", "CM000109.5");
        chromosomeMap.put("NC_006105.5", "CM000110.5");
        chromosomeMap.put("NC_006106.5", "CM000111.5");
        chromosomeMap.put("NC_006107.5", "CM000112.5");
        chromosomeMap.put("NC_006108.5", "CM000113.5");
        chromosomeMap.put("NC_006109.5", "CM000114.5");
        chromosomeMap.put("NC_006110.5", "CM000115.5");
        chromosomeMap.put("NC_006111.5", "CM000116.5");
        chromosomeMap.put("NC_006112.4", "CM000124.5");
        chromosomeMap.put("NC_006113.5", "CM000117.5");
        chromosomeMap.put("NC_006114.5", "CM000118.5");
        chromosomeMap.put("NC_006115.5", "CM000119.5");
        chromosomeMap.put("NC_028739.2", "CM003637.2");
        chromosomeMap.put("NC_028740.2", "CM003638.2");
        chromosomeMap.put("NC_006119.4", "CM000120.4");
        chromosomeMap.put("NC_008465.4", "CM000123.5");
        chromosomeMap.put("NC_006126.5", "CM000121.5");
        chromosomeMap.put("NC_006127.5", "CM000122.5");
        chromosomeMap.put("NC_001323.1", "X52392.1");

    }

    public void parse() throws IOException {

        FileReader genefr = null;
        BufferedReader geneReader = null;
        try {
            System.out.println("Parsing Gene File Name : " + geneFile.getName());
            genefr = new FileReader(geneFile);
            geneReader = new BufferedReader(genefr);
            String geneLine;
            int counter = 0;

            List<GeneBean> geneBeans = new ArrayList<>(RECORD_COUNT);
            for (int i = 0; i < RECORD_COUNT; i++) {
                GeneBean geneb = new GeneBean();
                geneBeans.add(geneb);
            }
            int index = 0;
            long start = System.currentTimeMillis();
            long localEnd, localStart;
            localStart = start;
            while ((geneLine = geneReader.readLine()) != null) {
                if (!geneLine.startsWith(HEADER_MARKER)) {

                    String[] geneFileds = geneLine.split("\t");
                    String geneExonCDS = geneFileds[2].trim();

                    if (!"gene".equals(geneExonCDS)) {
                        continue;
                    }

                    String chromosome = geneFileds[0];
                    chromosome = chromosomeMap.get(chromosome);
                    if (chromosome == null) {
                        continue;
                    }
//                    if (".1|".equalsIgnoreCase(chromosome)) {
//                        chromosome = "MT";
//                    }

//                    if (!isValidChromosome(chromosome)) {
//                        continue;
//                    }
                    String startPositionString = geneFileds[3].trim();
                    String endPositionString = geneFileds[4].trim();
                    long startPosition = Long.parseLong(startPositionString);
                    long endPosition = Long.parseLong(endPositionString);

                    String geneRecord = geneFileds[8];
                    String geneId = extractGeneId(geneRecord);

                    counter++;
                    GeneBean geneb = geneBeans.get(index);
                    index = index + 1;

                    geneb.setGeneId(geneId);
                    geneb.setChromosome(chromosome);
                    geneb.setStartPosition(startPosition);
                    geneb.setEndPosition(endPosition);

                    if ((index) % RECORD_COUNT == 0) {

                        storeGeneBean(geneBeans, index);

                        localEnd = System.currentTimeMillis();
                        //System.out.println("File Name : " + geneFile.getName() + "\tCounter : " + counter + "\t" + (localEnd - localStart) + " MilliSeconds");
                        localStart = System.currentTimeMillis();

                        for (int i = 0; i < RECORD_COUNT; i++) {
                            geneBeans.get(i).reset();
                        }

                        index = 0;
                    }

                }
            }

            storeGeneBean(geneBeans, index);
            for (int i = 0; i < RECORD_COUNT; i++) {
                geneBeans.get(i).reset();
            }
            long end = System.currentTimeMillis();
            System.out.println("Total Time " + (end - start) / 1000 + " in Seconds");
            // System.out.println("File Name : " + geneFile.getName());
            // System.out.println("Counter : " + counter);

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            Logger.getLogger(GeneParser_old.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                geneReader.close();
            } catch (IOException ex) {
                Logger.getLogger(GeneParser_old.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void storeGeneBean(List<GeneBean> beans, int count) {
        System.out.println("Count=" + count + "\t" + "GeneCount=" + beans.size());
        for (GeneBean bean : beans) {
            try {

                mongoDBGeneLoader.insert(bean);
                count--;
                if (count < 0) {
                    return;
                }

            } catch (Throwable t) {

                System.out.println("ERROR : " + bean.toString());
            }

        }
    }

    private String extractGeneId(String geneRecord) {

        String geneId = null;
        //gene_id "ENSGALG00000009771"; gene_source "ensembl"; gene_biotype "protein_coding"
//        String[] records = geneRecord.split(";");
//
//        String targetRecord = records[0];
//        geneId = targetRecord.substring(targetRecord.indexOf("\"") + 1, (targetRecord.lastIndexOf("\"")));
//        
        //  System.out.println("Gene :" + geneRecord);
        int idIndex = geneRecord.indexOf("GeneID");
        //  System.out.println("Gene ID :" + idIndex + "  : " + geneRecord.indexOf(":", idIndex) + "   , " + geneRecord.indexOf(";", idIndex));
        geneId = geneRecord.substring(geneRecord.indexOf(":", idIndex) + 1, geneRecord.indexOf(";", idIndex));
        return geneId;
    }

    private boolean isValidChromosome(String chromosome) {
        boolean valid = false;

        if (chromosome.equals("MT") || chromosome.equals("W") || chromosome.equals("Z")) {
            valid = true;
        } else {

            try {
                int chNumber = Integer.parseInt(chromosome);
                if (chNumber >= 1 && chNumber <= 32) {
                    valid = true;
                } else {
                    valid = false;
                }
            } catch (NumberFormatException nfe) {
                valid = false;
            }

        }
        return valid;
    }

    public static void main(String[] args) throws IOException {
//        String test = "1	protein_coding	gene	1735	16308	.	+	.	gene_id \"ENSGALG00000009771\"; gene_source \"ensembl\"; gene_biotype \"protein_coding\"";
//
//        String[] sa = test.split("\t");
//        for (String s : sa) {
//            System.out.println(s);
//        }

        //String geneId = extractGeneId(sa[8]);
        //System.out.println(geneId);
        //   String geneFilePath = "/home/sandeep/Desktop/Gallus_gallus.Galgal4.76.gtf";
        String geneFilePath = "/Users/ramki/Desktop/GCF_000002315.5_GRCg6a_genomic.gff";
        File geneFile = new File(geneFilePath);
        GeneParser geneParser = new GeneParser(geneFile, null);
        geneParser.parse();

    }

}
