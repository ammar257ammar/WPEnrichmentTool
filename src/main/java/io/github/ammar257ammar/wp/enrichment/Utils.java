package io.github.ammar257ammar.wp.enrichment;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bridgedb.Xref;
import org.pathvisio.data.IRow;
import org.pathvisio.desktop.visualization.Criterion;
import org.pathvisio.desktop.visualization.Criterion.CriterionException;
import org.pathvisio.statistics.PathwayMap;
import org.pathvisio.statistics.StatisticsResult;

public class Utils {
	
	public static Map<Xref, Set<String>> getGlobalPositiveGenes(Criterion criteria, StatisticsResult statistics) {

		Map<Xref, Set<String>> dataMap = new HashMap<Xref, Set<String>>();
		
		PathwayMap pwyMap = new PathwayMap(new File(Constant.PATHWAYS_DIR));

		Set<Xref> refs = pwyMap.getSrcRefs();

		for (Xref srcRef : refs) {

			Set<String> cGenePositive = new HashSet<String>();

			List<? extends IRow> rows = statistics.getGex().getData(srcRef);

			if (rows != null) {
				for (IRow row : rows) {

					try {
						@SuppressWarnings("deprecation")
						boolean eval = criteria.evaluate(row.getByName());
						if (eval)
							cGenePositive.add(row.getXref().getId());
					} catch (CriterionException e) {
						System.out.println("Error with statistics");
					}
				}

			}
			dataMap.put(srcRef, cGenePositive);
		}
		return dataMap;
	}
}
