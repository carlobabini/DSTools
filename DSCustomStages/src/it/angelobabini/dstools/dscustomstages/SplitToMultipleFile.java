package it.angelobabini.dstools.dscustomstages;

import com.ascentialsoftware.jds.Row;
import com.ascentialsoftware.jds.Stage;

public class SplitToMultipleFile extends Stage {
	/**
	 * Carattere "meno", memorizzato per motivi di performance
	 */
	public static char minus_char = '-';

	/**
	 * Stringa "+", memorizzata per motivi di performance
	 */
	public static String plus = "+";

	/**
	 * Stringa "-", memorizzata per motivi di performance
	 */
	public static String minus = "-";

	/**
	 * Lista dei "tipi codice" da prendere in considerazione:<br/>
	 * <ol>
	 * <li>0 = rettifiche</li>
	 * <li>1 = clienti (SCARTATI)</li>
	 * <li>2 = fornitori</li>
	 * <li>3 = reparti</li>
	 * </ol>
	 */
	public static String[] tipi_codice = {"0", "2", "3"};

	/**
	 * Legge le righe (una sola, per definizione della TABCLI236 3009) e le esplode
	 * su più righe:<br>
	 * in ingresso legge solo la colonna 0 - RESTO (dove ci sono tutti i dati)<br>
	 * in uscita le tre colonne<br>
	 * <sl>
	 * <li>colonna 0 - TIPO_CODICE int 1 cifra (vedi tipi_codice)</li>
	 * <li>colonna 1 - CAUSALE int 3 cifre</li>
	 * <li>colonna 2 - SEGNO char 1 cifra (+ o -)</li>
	 * </sl>
	 *
	 * Siccome i valori sono impostati in intervalli, per ogni intervallo viene
	 * esploso ogni singolo valore, ad esempio:
	 * l'intervallo 101104+ genera le righe
	 * 0, 101, +
	 * 0, 102, +
	 * 0, 103, +
	 * 0, 104, +
	 * 2, 101, +
	 * 2, 102, +
	 * 2, 103, +
	 * 2, 104, +
	 * 3, 101, +
	 * 3, 102, +
	 * 3, 103, +
	 * 3, 104, +
	 */
	@Override
	public int process() {
		Row inputRow = readRow();
		if (inputRow == null) {
			return Stage.OUTPUT_STATUS_END_OF_DATA;
		}

		char[] resto = inputRow.getValueAsString(0).toCharArray();
		for(int i=0; i+7<=resto.length; i+=7) {
			int causale_begin = Integer.parseInt(String.valueOf(resto, i+0, 3));
			int causale_end = Integer.parseInt(String.valueOf(resto, i+3, 3));
			char segno = resto[i+6];

			if(causale_begin+causale_end>0) {
				for(int j=causale_begin; j<=causale_end; j++) {
					for(int k=0; k<tipi_codice.length; k++) {
						Row outputRow = createOutputRow();
						outputRow.setValueAsString(0, tipi_codice[k]);
						outputRow.setValueAsString(1, String.valueOf(j));
						outputRow.setValueAsString(2, segno==minus_char ? minus : plus);
						writeRow(outputRow);
					}
				}
			}
		}
		return Stage.OUTPUT_STATUS_READY;
	}
}
