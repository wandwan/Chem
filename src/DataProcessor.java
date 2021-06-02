import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class DataProcessor {
	File f;
	File output;
	ArrayList<String> data;
	ArrayList<Double> nums;
	ArrayList<String> names;
	double[] weights;
	ArrayList<Double> mass;
	ArrayList<Monomer> ans;
	double precision;
	public DataProcessor(File f) {
		this.f = f;
		data = new ArrayList<String>();
		nums = new ArrayList<Double>();
		names = new ArrayList<String>();
		mass = new ArrayList<Double>();
		ans = new ArrayList<Monomer>();
	}

	public void init() throws Exception{
		try {
			XSSFWorkbook wb = new XSSFWorkbook(f);
			XSSFSheet sheet = wb.getSheetAt(0);
			Iterator<Row> rt = sheet.rowIterator();
			DataFormatter df = new DataFormatter();
			Iterator<Cell> ct;
			Row r;
			for (int i = 0; i < 2; i++) {
				r = rt.next();
				if (isRowEmpty(r)) {
					i--;
					continue;
				}
				ct = r.cellIterator();
				ct.next();
				data.add(df.formatCellValue(ct.next()));
			}
			for (int i = 0; i < 4; i++) {
				r = rt.next();
				if (isRowEmpty(r)) {
					i--;
					continue;
				}
				ct = r.cellIterator();
				ct.next();
				nums.add(ct.next().getNumericCellValue());
			}
			rt.next();
			ct = rt.next().cellIterator();
			ct.next();
			data.add(df.formatCellValue(ct.next()));
			rt.next();
			rt.next();
			ct = rt.next().cellIterator();
			ct.next();
			while (ct.hasNext()) {
				Cell c = ct.next();
				if (c.getCellType() == CellType.STRING)
					names.add(c.getStringCellValue());
				else
					break;
			}
			weights = new double[names.size()];
			ct = rt.next().cellIterator();
			ct.next();
			for (int i = 0; i < weights.length; i++)
				weights[i] = ct.next().getNumericCellValue();
			rt.next();
			rt.next();
			while (rt.hasNext()) {
				ct = rt.next().cellIterator();
				ct.next();
				if (ct.hasNext())
					mass.add(ct.next().getNumericCellValue());
				if (mass.get(mass.size() - 1) == 0.0)
					mass.remove(mass.size() - 1);
			}
			precision = 1;
			HashMap<String, Double> map = new HashMap<String, Double>();
			map.put("tenth", 10.0);
			map.put("tens", .1);
			map.put("hundredth", 100.0);
			map.put("thousandth", 1000.0);
			String regExp = "[\\x00-\\x20]*[+-]?(((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)([eE][+-]?(\\p{Digit}+))?)|(\\.((\\p{Digit}+))([eE][+-]?(\\p{Digit}+))?)|(((0[xX](\\p{XDigit}+)(\\.)?)|(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+)))[pP][+-]?(\\p{Digit}+)))[fFdD]?))[\\x00-\\x20]*";
			if (data.get(data.size() - 1).matches(regExp))
				precision = 1 / Double.parseDouble(data.get(data.size() - 1));
			else if (map.containsKey(data.get(data.size() - 1).toLowerCase()))
				precision = map.get(data.get(data.size() - 1).toLowerCase());

//			System.out.println(Arrays.toString(weights));
//			System.out.println(precision + " " + (mass.get(0) - nums.get(0) - nums.get(1) - nums.get(2)) + " ");
			if(precision == 0)
				precision = 1;
			for (int i = 0; i < mass.size(); i++)
				ans.add(new Monomer(mass.get(i) - nums.get(0) - nums.get(1) - nums.get(2), weights, precision,
						nums.get(3)));
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}
		output();
	}

	public void output() throws Exception {
		String home = System.getProperty("user.home");
		try {
			XSSFWorkbook wb = new XSSFWorkbook();
			XSSFSheet sheet = wb.createSheet();
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
			DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			Object[][] bookData = { 
					{ "Date", dtf.format(now), "", "Project Name", data.get(0)}, 
					{""},
					{ "Time", dtf1.format(now), "", "Polymer Name", data.get(1)},
					{""},
					{ "Tolerance", nums.get(3), "", "Ion Weight", nums.get(0)}, 
					{""},
					{ "", "", "", "Start Group Weight", nums.get(1)},
					{""},
					{ "", "", "", "End Group Weight", nums.get(2)},
					{""},
					{""},
					{},
					{"", "", "", "Monomer Names", names},
					{"", "", "", "Monomer Weights", weights},
					{""},
					{""},
					{},
					{"Peak Number", "Mass", "Adjusted Mass", "delta", names, "Real Mass", "Ion Added"}
			};
			bookData[11] = new Object[weights.length + 4];
			for(int i = 0; i < 4; i++)
				bookData[11][i] = "";
			for(int i = 0; i < weights.length; i++)
				bookData[11][4 + i] = ((char) ('A' + i));
			bookData[16] = new Object[weights.length + 4];
			for(int i = 0; i < 4; i++)
				bookData[16][i] = "";
			for(int i = 0; i < weights.length; i++)
				bookData[16][4 + i] = ((char) ('A' + i));
			int rowCount = 0;
			int columnMax = 0;
			for (Object[] aBook : bookData) {
				Row row = sheet.createRow(++rowCount);
				int columnCount = 0;
				for (Object field : aBook) {
					Cell cell = row.createCell(++columnCount);
					if (field instanceof String) {
						cell.setCellValue((String) field);
					} else if (field instanceof Double) {
						cell.setCellValue((double) field);
					} else if(field instanceof ArrayList) {
						for(int i = 0; i < ((ArrayList) field).size(); i++) {
							cell.setCellValue((String) (((ArrayList) field).get(i)));
							if(i != ((ArrayList) field).size() - 1)
							cell = row.createCell(++columnCount);
						}
					} else if(field instanceof double[]) {
						for(int i = 0; i < ((double[]) field).length; i++) {
							cell.setCellValue((((double[]) field)[i]));
							cell = row.createCell(++columnCount);
						}
					}
					columnMax = Math.max(columnMax, columnCount);
				}
			}
			int prev = 0;
			for(int i = 0; i < ans.size(); i++) {
//				System.out.println(ans);
				int delta = 0;
//				System.out.println(tans);
				while(delta < ans.get(i).ans.length) {
					ArrayList<Temp> tans = ans.get(i).ans[delta];
//					System.out.println(tans);
					for(int j = 0; j < tans.size(); j++) {
						Row row = sheet.createRow(++rowCount);
						int columnCount = 0;
						Cell cell = row.createCell(++columnCount);
						if(prev != i + 1) {
							prev = i + 1;
							cell.setCellValue(i + 1);
						}
						cell = row.createCell(++columnCount);
						cell.setCellValue(mass.get(i));
						cell = row.createCell(++columnCount);
						cell.setCellValue(mass.get(i) - nums.get(0) - nums.get(1) - nums.get(2));
						cell = row.createCell(++columnCount);
						cell.setCellValue(delta / precision);
						for(int k = 0; k < weights.length; k++) {
							cell = row.createCell(++columnCount);
							cell.setCellValue(tans.get(j).a[k]);
						}
						cell = row.createCell(++columnCount);
						cell.setCellValue(tans.get(j).a[weights.length] / precision);
						cell = row.createCell(++columnCount);
						cell.setCellValue(nums.get(0) + (tans.get(j).a[weights.length] / precision));
					}
					delta++;
				}
			}
			for(int i = 0; i < columnMax; i++)
				sheet.autoSizeColumn(i);
			int off = 0;
			File f = new File(Runner.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath() + data.get(0) + ".xlsx");
			while(f.exists()) {
				f = new File(Runner.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath() + data.get(0) + off + ".xlsx");
				off++;
			}
			FileOutputStream fos = new FileOutputStream(f.getPath());
			wb.write(fos);
			fos.close();
			wb.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static boolean isRowEmpty(Row row) {
		for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
			Cell cell = row.getCell(c);
			if (cell != null && cell.getCellType() != CellType.BLANK)
				return false;
		}
		return true;
	}
}
