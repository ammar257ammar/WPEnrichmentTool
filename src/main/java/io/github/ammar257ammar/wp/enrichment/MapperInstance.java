package io.github.ammar257ammar.wp.enrichment;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.bridgedb.BridgeDb;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.pathvisio.core.preferences.GlobalPreference;
import org.pathvisio.core.preferences.PreferenceManager;

public class MapperInstance {

	public static Map<String, String> getDbMap() {

		Map<String, String> map = new HashMap<String, String>();

		File dbRoot = new File(Constant.DB_DIR);

		if (dbRoot.exists() && dbRoot.isDirectory()) {

			String[] dbFiles = dbRoot.list();

			map.put("homo_sapiens", Arrays.asList(dbFiles).stream().filter(o -> o.startsWith("Hs_"))
					.max(Comparator.comparing(c -> Integer.valueOf(c.substring(c.lastIndexOf("_") + 1, c.indexOf("."))))).orElse(""));

			map.put("mus_musculus", Arrays.asList(dbFiles).stream().filter(o -> o.startsWith("Mm_"))
					.max(Comparator.comparing(c -> Integer.valueOf(c.substring(c.lastIndexOf("_") + 1, c.indexOf("."))))).orElse(""));

			map.put("rattus_norvegicus", Arrays.asList(dbFiles).stream().filter(o -> o.startsWith("Rn_"))
					.max(Comparator.comparing(c -> Integer.valueOf(c.substring(c.lastIndexOf("_") + 1, c.indexOf("."))))).orElse(""));
			
			map.put("bos_taurus", Arrays.asList(dbFiles).stream().filter(o -> o.startsWith("Bt_"))
					.max(Comparator.comparing(c -> Integer.valueOf(c.substring(c.lastIndexOf("_") + 1, c.indexOf("."))))).orElse(""));
			
			map.put("caenorhabditis_elegans", Arrays.asList(dbFiles).stream().filter(o -> o.startsWith("Ce_"))
					.max(Comparator.comparing(c -> Integer.valueOf(c.substring(c.lastIndexOf("_") + 1, c.indexOf("."))))).orElse(""));
			
			map.put("canis_familiaris", Arrays.asList(dbFiles).stream().filter(o -> o.startsWith("Cf_"))
					.max(Comparator.comparing(c -> Integer.valueOf(c.substring(c.lastIndexOf("_") + 1, c.indexOf("."))))).orElse(""));
			
			map.put("ciona_intestinalis", Arrays.asList(dbFiles).stream().filter(o -> o.startsWith("Ci_"))
					.max(Comparator.comparing(c -> Integer.valueOf(c.substring(c.lastIndexOf("_") + 1, c.indexOf("."))))).orElse(""));
			
			map.put("drosophila_melanogaster", Arrays.asList(dbFiles).stream().filter(o -> o.startsWith("Dm_"))
					.max(Comparator.comparing(c -> Integer.valueOf(c.substring(c.lastIndexOf("_") + 1, c.indexOf("."))))).orElse(""));
			
			map.put("danio_rerio", Arrays.asList(dbFiles).stream().filter(o -> o.startsWith("Dr_"))
					.max(Comparator.comparing(c -> Integer.valueOf(c.substring(c.lastIndexOf("_") + 1, c.indexOf("."))))).orElse(""));
			
			map.put("gallus_gallus", Arrays.asList(dbFiles).stream().filter(o -> o.startsWith("Gg_"))
					.max(Comparator.comparing(c -> Integer.valueOf(c.substring(c.lastIndexOf("_") + 1, c.indexOf("."))))).orElse(""));
			
			map.put("macaca_mulatta", Arrays.asList(dbFiles).stream().filter(o -> o.startsWith("Ml_"))
					.max(Comparator.comparing(c -> Integer.valueOf(c.substring(c.lastIndexOf("_") + 1, c.indexOf("."))))).orElse(""));
			
			map.put("ornithorhynchus_anatinus", Arrays.asList(dbFiles).stream().filter(o -> o.startsWith("Oa_"))
					.max(Comparator.comparing(c -> Integer.valueOf(c.substring(c.lastIndexOf("_") + 1, c.indexOf("."))))).orElse(""));
			
			map.put("pan_troglodytes", Arrays.asList(dbFiles).stream().filter(o -> o.startsWith("Pt_"))
					.max(Comparator.comparing(c -> Integer.valueOf(c.substring(c.lastIndexOf("_") + 1, c.indexOf("."))))).orElse(""));
			
			map.put("equus_caballus", Arrays.asList(dbFiles).stream().filter(o -> o.startsWith("Qc_"))
					.max(Comparator.comparing(c -> Integer.valueOf(c.substring(c.lastIndexOf("_") + 1, c.indexOf("."))))).orElse(""));
			
			map.put("sus_scrofa", Arrays.asList(dbFiles).stream().filter(o -> o.startsWith("Ss_"))
					.max(Comparator.comparing(c -> Integer.valueOf(c.substring(c.lastIndexOf("_") + 1, c.indexOf("."))))).orElse(""));
			
			map.put("saccharomyces_cerevisiae", Arrays.asList(dbFiles).stream().filter(o -> o.startsWith("Sc_"))
					.max(Comparator.comparing(c -> Integer.valueOf(c.substring(c.lastIndexOf("_") + 1, c.indexOf("."))))).orElse(""));
		}

		return map;
	}

	private MapperInstance() {}

	public synchronized static IDMapper init(String organism) {

		organism = organism.trim().toLowerCase().replace(" ","_");

		Map<String, String> map = getDbMap();
		
		String dbPath = map.get(organism);
		
		if("".equals(dbPath)) {
			
			try {
				throw new IDMapperException();
			} catch (IDMapperException e1) {
				System.out.println("Error initiating IDMapper");
				System.out.println("Please make sure the /db path is mapped properly and the BridgeDB db of your target species is located in it");
			}
		}else {
			System.out.println("Selected BridgeDB database: " + dbPath);
		}
			
		
		PreferenceManager.getCurrent().set(GlobalPreference.DB_CONNECTSTRING_GDB, ("idmapper-pgdb:" + Constant.DB_DIR + "/" + dbPath));

		IDMapper mapper = null;
		try {
			mapper = BridgeDb.connect("idmapper-pgdb:" + Constant.DB_DIR + "/" + dbPath);
		} catch (IDMapperException e) {
			System.out.println("Error initiating IDMapper");
			System.out.println("Please make sure the /db path is mapped properly to your BridgeDB derby files directory");
		}
		
		return mapper;
	}
}
