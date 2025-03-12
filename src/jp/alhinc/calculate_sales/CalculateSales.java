package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// 商品定義ファイル名
	private static final String FILE_NAME_COMMODITY_LST = "commodity.lst";

	// 商品別集計ファイル名
	private static final String FILE_NAME_COMMODITY_OUT = "commodity.out";

	// エラーメッセージ
	private static final String BRANCH = "支店";
	private static final String COMMODITY = "商品";
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "定義ファイルのフォーマットが不正です";
	private static final String FILE_NOT_CONSECUTIVE = "売り上げファイル名が連番になっていません";
	private static final String SALEAMOUNT_OVER = "合計金額が10桁を超えました";
	private static final String BRANCHCODE_ILLEGAL= "の支店コードが不正です";
	private static final String COMMODITYCODE_ILLEGAL = "の商品コードが不正です";
	private static final String SALESFILE_NOT_CONSECUTIVE = "のフォーマットが不正です";



	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		//コマンドライン引数が渡されているか確認
		if(args.length != 1) {
			System.out.println(UNKNOWN_ERROR);
			return;
		}

		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();
		// 商品コードと商品名を保持するMap
		Map<String, String> commodityNames = new HashMap<>();
		// 商品コードと売上金額を保持するMap
		Map<String, Long> commoditySales = new HashMap<>();
		//支店コードの正規表現
		String branchRegex = "^[0-9]{3}$";
		//商品コードの正規表現
		String commodityRegex = "^[A-Za-z0-9]{8}$";

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales, branchRegex, BRANCH)) {
			return;
		}
		//商品定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_COMMODITY_LST, commodityNames, commoditySales, commodityRegex, COMMODITY)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		File[] files = new File(args[0]).listFiles();
		List<File> rcdFiles = new ArrayList<>();

		for(int i = 0; i < files.length ; i++) {
			 //対象がファイルであり、「数字8桁.rcd」なのか確認
			if(files[i].isFile() && files[i].getName().matches("^[0-9]{8}[.]rcd$")) {
				rcdFiles.add(files[i]);
			}
		}

		Collections.sort(rcdFiles);
		//ファイルが連番になっているか確認
		for(int i = 0; i < rcdFiles.size() - 1; i++) {

			int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
			int latter = Integer.parseInt(rcdFiles.get(i + 1).getName().substring(0, 8));

		      //比較する2つのファイル名の先頭から数字の8文字を切り出し、int型に変換します。
			if((latter - former) != 1) {
				//2つのファイル名の数字を比較して、差が1ではなかったら、
				//エラーメッセージをコンソールに表示します。
				System.out.println(FILE_NOT_CONSECUTIVE);
				return;
			}
		}


		for(int i = 0; i < rcdFiles.size(); i++) {
			//支店・商品定義ファイル読み込み(readFileメソッド)を参考に売上ファイルの中身を読み込みます。
			BufferedReader br = null;
			try {
				FileReader fr = new FileReader(rcdFiles.get(i));
				br = new BufferedReader(fr);

				List<String> branchData = new ArrayList<>();

				String line;
				String fileName = rcdFiles.get(i).getName();

				while((line = br.readLine()) != null) {
					branchData.add(line);
				}

				//売上ファイルの中身が3行かどうかを確認
				if(branchData.size() != 3) {
					System.out.println(fileName + SALESFILE_NOT_CONSECUTIVE);
					return;
				}

				//支店情報を保持しているMapに売上ファイルの支店コードが存在するか確認
				if (!branchNames.containsKey(branchData.get(0))) {
				    System.out.println(fileName + BRANCHCODE_ILLEGAL);
				    return;
				}

				//商品情報を保持しているMapに商品ファイルの商品コードが存在するか確認
				if (!commodityNames.containsKey(branchData.get(1))) {
				    System.out.println(fileName + COMMODITYCODE_ILLEGAL);
				    return;
				}


				//売上金額が数字かどうか確認
				if(!branchData.get(2).matches("^[0-9]+$")) {
					System.out.println(UNKNOWN_ERROR);
					return;
				}

				long fileSale = Long.parseLong(branchData.get(2));

				Long branchSaleAmount = branchSales.get(branchData.get(0)) + fileSale;
				Long commoditySaleAmount = commoditySales.get(branchData.get(1)) + fileSale;
				//売上⾦額の合計が10桁を超えたか確認
				if(branchSaleAmount >= 10000000000L || commoditySaleAmount >= 10000000000L){
					System.out.println(SALEAMOUNT_OVER);
					return;
				}
				branchSales.put(branchData.get(0), branchSaleAmount);
				commoditySales.put(branchData.get(1), commoditySaleAmount);

			} catch(IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return;
			} finally {
				if(br != null) {
					// ファイルを開いている場合
					try {
						// ファイルを閉じる
						br.close();
					} catch(IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}
		}

		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

		//商品別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_COMMODITY_OUT, commodityNames, commoditySales)) {
			return;
		}

	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> codeNamesMap
			, Map<String, Long> codeSalesMap, String regex, String category) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			//ファイルの存在チェック
			if(!file.exists()) {
				System.out.println(category + FILE_NOT_EXIST);
				return false;
			}
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				String[] items = line.split(",");

				if((items.length != 2) || (!items[0].matches(regex))) {
					System.out.println(category + FILE_INVALID_FORMAT);
					return false;
				}
				codeNamesMap.put(items[0], items[1]);
				codeSalesMap.put(items[0], 0L);
			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> codeNamesMap
			, Map<String, Long> codeSalesMap) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		BufferedWriter bw = null;

		try {
			File file = new File(path, fileName);
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);

			for(String key : codeNamesMap.keySet()) {
				bw.write(key + "," + codeNamesMap.get(key) + "," + codeSalesMap.get(key));
				bw.newLine();
			}
		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

}
