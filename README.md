# WPEnrichmentTool

![GitHub top language](https://img.shields.io/github/languages/top/ammar257ammar/WPEnrichmentTool) ![Lines of code](https://img.shields.io/tokei/lines/github/ammar257ammar/WPEnrichmentTool) ![GitHub](https://img.shields.io/github/license/ammar257ammar/WPEnrichmentTool) ![GitHub release (latest by date)](https://img.shields.io/github/v/release/ammar257ammar/WPEnrichmentTool) [![Dockerhub](https://img.shields.io/badge/dockerhub-aammar%2Fwp--enrichment--tool-green)](https://hub.docker.com/r/aammar/wp-enrichment-tool) ![Docker Image Size (tag)](https://img.shields.io/docker/image-size/aammar/wp-enrichment-tool/latest) [![DOI](https://zenodo.org/badge/484464996.svg)](https://zenodo.org/badge/latestdoi/484464996)

WPEnrichmentTool is a java command line tool to perform pathway enrichment (over-representation analysis) on differential gene expression contrasts using WikiPathways pathways. 

This tool uses the [PathVisio](https://github.com/PathVisio/pathvisio) v3.3.0 as a dependency library for pathway enrichment and the [BridgeDB](https://github.com/bridgedb/BridgeDb) dependency for gene identifier mapping.

**NOTE:** WPEnrichmentTool supports only Ensembl gene IDs. Therefore, the gene IDs in the differential gene expression contrast should be provided as Ensembl gene IDs.

WPEnrichmentTool was developed as part of the NanoLinksKG project to empower the constructed knowledge graph from nanosafety data with pathway enrichment results besides the differentially expressed genes obtained from transcriptomics experiments of nanomaterial treatments.

To build the docker image of WPEnrichmentTool, please follow the instructions.



## Docker Hub image

The Docker image of WPEnrichmentTool is available on DockerHub and ready-to-use. All what you need to do is to pull the image and you are good to go. To pull the image use the following command:

```bash
docker pull aammar/wp-enrichment-tool:latest
```



## Build the Docker

- First, clone this repository and "cd" to its directory.

- Second, build the Docker image

```bash
docker build -t wp-enrichment-tool .
```

### 

## Usage

```bash
docker run -it --rm --name wpet \
	-v PATH_TO_PATHWAYS:/pathways \
	-v PATH_TO_BRIDGE_DB_DERBY_FILES:/db \
	-v PATH_TO_DATA:/data
	aammar/wp-enrichment-tool PATH_TO_DEG_CONTRAST_CSV ORGANISM_NAME EXPRESSION GENE_ID_COLUMN_INDEX
```

The placeholders in the previous command should be replace as follows:

- PATH_TO_PATHWAYS: is the directory of WikiPathways pathways. You can download the latest version of the pathways from https://www.wikipathways.org/index.php/Download_Pathways and unzip the file to the directory PATH_TO_PATHWAYS.

- PATH_TO_BRIDGE_DB_DERBY_FILES: is the directory where BridgeDB Derby database files are located. You can download the database related to your target species from https://zenodo.org/record/5970988#.YmKTlWgzaUl (Ensembl v103, February 2022). If you have multiple versions of the database for the same species, the tool will automatically select the most recent version.

- PATH_TO_DATA: is the directory where you differential gene expression contrast CSV files are located.

- PATH_TO_DEG_CONTRAST_CSV: is the path to the contrast CSV file that you want to get the enriched pathways for. **Note** that the path to the file should always start with "/data" since this is where you mapped your PATH_TO_DATA folder to.

- ORGANISM_NAME: is the species name used by the tool to select and load the proper BridgeDB database. The value of this argument can be one of the following 16 species supported by WPEnrichmentTool:

  homo_sapiens
  mus_musculus
  rattus_norvegicus
  bos_taurus
  caenorhabditis_elegans
  canis_familiaris
  ciona_intestinalis
  drosophila_melanogaster
  danio_rerio
  gallus_gallus
  macaca_mulatta
  ornithorhynchus_anatinus
  pan_troglodytes
  equus_caballus
  sus_scrofa
  saccharomyces_cerevisiae

- EXPRESSION: this is the evaluation criterial needed to calculate the z-score. Example: "[logFC] > 0.58 AND [adj.P.Val] < 0.05". In this example, logFC and adj.P.Val are the column names in the input CSV file. **Note** that you need to wrap the column names with square brackets (like in **[**logFC**]**).

- GENE_ID_COLUMN_INDEX is the index of the column of Gene IDs in you contrast CSV file. This argument is optional if your gene ID column is the first column. In this case, the value "0" will be used to point to the first column. If your gene ID column index is not "0" (i.e. the first column in the CSV), then provide the index of that column.

## Example output file

| pathway                                                      | z-score | p-value | numOfPositives* | geneList*                                                    |
| ------------------------------------------------------------ | ------- | ------- | --------------- | ------------------------------------------------------------ |
| Zinc homeostasis                                             | 12.67   | 0.000   | 8               | ENSG00000169688&#124;ENSG00000260549&#124;ENSG00000205358&#124;ENSG00000205364&#124;ENSG00000125144&#124;ENSG00000198417&#124;ENSG00000169715 |
| Copper homeostasis                                           | 10.80   | 0.000   | 9               | ENSG00000169688&#124;ENSG00000255986&#124;ENSG00000260549&#124;ENSG00000205358&#124;ENSG00000177606&#124;ENSG00000198417&#124;ENSG00000125144 |
| TNF related weak inducer of apoptosis (TWEAK) Signaling Pathway | 5.05    | 0.000   | 4               | ENSG00000077150&#124;ENSG00000006327&#124;ENSG00000136244&#124;ENSG00000177606 |
| Cytokines and Inflammatory Response                          | 4.44    | 0.000   | 2               | ENSG00000136244&#124;ENSG00000081041                         |
| Photodynamic therapy-induced NF-kB survival signaling        | 4.37    | 0.000   | 3               | ENSG00000077150&#124;ENSG00000136244&#124;ENSG00000081041    |
| H19 action Rb-E2F1 signaling and CDK-Beta-catenin activity   | 4.26    | 0.000   | 2               | ENSG00000141682&#124;ENSG00000101384                         |

\* **numberOfPositives** is the number of genes which are assessed positive for the evaluation expression, while the **geneList** column provides the actual Ensembl IDs for those genes separated by '|' character.  

