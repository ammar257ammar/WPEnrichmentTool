package io.github.ammar257ammar.wp.enrichment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bridgedb.DataSource;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.IDMapperStack;
import org.bridgedb.Xref;
import org.bridgedb.rdb.construct.DataDerby;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.data.DataException;
import org.pathvisio.desktop.gex.GexManager;
import org.pathvisio.desktop.gex.SimpleGex;
import org.pathvisio.desktop.visualization.Criterion;
import org.pathvisio.gexplugin.GexTxtImporter;
import org.pathvisio.gexplugin.ImportInformation;
import org.pathvisio.statistics.Column;
import org.pathvisio.statistics.PathwayMap;
import org.pathvisio.statistics.PathwayMap.PathwayInfo;
import org.pathvisio.statistics.StatisticsPathwayResult;
import org.pathvisio.statistics.StatisticsResult;
import org.pathvisio.statistics.ZScoreCalculator;

public class App {

	public static void main(String[] args) {

		Logger.log.setLogLevel(false, false, false, false, false, false);
		
		if (args.length < 3) {
			System.out.println("You need to provide 3 arguments at least:");
			System.out.println("1- input CSV file path");
			System.out.println("2- organism name to load the proper BirdgeDB (e.g. homo_sapiens)");
			System.out.println("3- criteria for evaluation (typically an expression like: [logFC] > 0.58 AND [adj.P.Val] < 0.05");
			System.out.println("4- (OPTIONAL) Gene ID column zero-based index (default to the first column, default value: 0)");

			System.exit(0);
		}

		File inputFile = new File(args[0]);

		if (!inputFile.exists()) {
			System.out.println("input file does not exist");
			System.exit(0);
		}

		Set<String> organisms = MapperInstance.getDbMap().keySet();
		String organism = args[1];

		if (organisms.stream().noneMatch(organism::equals)) {
			System.out.println("oganism name should be one of: \n" + String.join("\n", organisms));
			System.exit(0);
		}

		String exprZ = args[2];

		if (args.length == 4) {

			try {

				pathwayEnrichmentFromContrast(inputFile, organism, exprZ, Integer.valueOf(args[3]));

			} catch (NumberFormatException ex) {

				System.out.println("Unrecognized value of gene ID column index, it should be and integer >= 0");
				System.out.println("Trying to use column index 0 ...");
				pathwayEnrichmentFromContrast(inputFile, organism, exprZ);
			}

		} else {
			System.out.println("Gene ID column index is not provided, using the default value: 0");
			pathwayEnrichmentFromContrast(inputFile, organism, exprZ);
		}

	}

	public static void pathwayEnrichmentFromContrast(File inputFile, String organism, String exprZ) {
		pathwayEnrichmentFromContrast(inputFile, organism, exprZ, 0);
	}

	public static void pathwayEnrichmentFromContrast(File inputFile, String organism, String exprZ, int idColIndex) {

		try {

			Config.init();
			Config.initFolders(inputFile.getParent());

			IDMapper mapper = MapperInstance.init(organism);
			IDMapperStack currentGdb = new IDMapperStack();

			currentGdb.setTransitive(true);
			currentGdb.addIDMapper(mapper);

			String contrast = inputFile.getName().substring(0, inputFile.getName().lastIndexOf('.'));
			String pgexPath = inputFile.getParent() + "/pgex/" + contrast + ".pgex";
			String outPath = inputFile.getParent() + "/pathways/" + contrast + ".csv";

			ImportInformation importInformation = new ImportInformation();

			importInformation.setDataSource(DataSource.getExistingBySystemCode("En"));
			importInformation.setTxtFile(inputFile);
			importInformation.setGexName(pgexPath);
			importInformation.setDelimiter(",");
			importInformation.setSyscodeFixed(true);
			importInformation.setIdColumn(idColIndex);
			importInformation.setFirstHeaderRow(0);
			importInformation.setFirstDataRow(1);

			GexManager gex = new GexManager();

			GexTxtImporter.importFromTxt(importInformation, null, // OR new ProgressKeeper((int)1E6),
					currentGdb, gex);

			SimpleGex simpleGex = new SimpleGex(pgexPath, false, new DataDerby());

			gex.setCurrentGex(simpleGex);

			File pwDir = new File(Constant.PATHWAYS_DIR);

			Criterion criteria = new Criterion();
			criteria.setExpression(exprZ, simpleGex.getSampleNames());
			ZScoreCalculator zsc = new ZScoreCalculator(criteria, pwDir, gex.getCachedData(), mapper, null);

			StatisticsResult statistics = zsc.calculateMappFinder();

			Map<Xref, Set<String>> dataMap = Utils.getGlobalPositiveGenes(criteria, statistics);

			PathwayMap pwyMap = new PathwayMap(new File(Constant.PATHWAYS_DIR));

			List<StatisticsPathwayResult> results = statistics.getPathwayResults();

			List<String[]> outRows = new ArrayList<String[]>();

			for (StatisticsPathwayResult res : results) {

				if (Double.parseDouble(res.getProperty(Column.ZSCORE)) > 0.0 && Integer.parseInt(res.getProperty(Column.R)) > 0
						&& Double.parseDouble(res.getProperty(Column.ADJPVAL)) < 0.1) {

					Set<String> probesPositive = new HashSet<String>();

					for (PathwayInfo pi : pwyMap.getPathways()) {

						if (pi.getName().equals(res.getProperty(Column.PATHWAY_NAME))) {

							for (Xref ref : pi.getSrcRefs()) {
								probesPositive.addAll(dataMap.get(ref));
							}
						}
					}

					outRows.add(
							new String[] { res.getProperty(Column.PATHWAY_NAME).replace(',', ';'), res.getProperty(Column.ZSCORE),
									res.getProperty(Column.ADJPVAL), res.getProperty(Column.R), String.join("|", probesPositive) });
				}
			}

			if (outRows.size() > 0) {

				try (BufferedWriter bw = new BufferedWriter(new FileWriter(outPath))) {

					bw.write(String.join(",", new String[] { "pathway", "z-score", "p-value", "numOfPositives", "geneList" }));
					bw.newLine();

					for (String[] row : outRows) {
						bw.write(String.join(",", row));
						bw.newLine();
					}
				}
				
				System.out.println("Process finished. Number of resulting pathways: " + String.valueOf(outRows.size()));
			}else {
				System.out.println("No pathways were enriched");
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (IDMapperException e) {
			e.printStackTrace();
		} catch (DataException e) {
			e.printStackTrace();
		}
	}

}
