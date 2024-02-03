package mikeshafter.iciwi.util;

import org.dhatim.fastexcel.reader.Cell;
import org.dhatim.fastexcel.reader.CellType;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import mikeshafter.iciwi.Iciwi;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;
public class ExcelHelper {

private final HashSet<int[]> processedCells = new HashSet<>();
private Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private  Logger logger = plugin.getLogger();

public Set<Cell[][]> readExcel (String fileName) throws IOException
{
	if (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls")) throw new IOException("Not an Excel file!");
	FileInputStream file = new FileInputStream(fileName);
	ReadableWorkbook wb = new ReadableWorkbook(file);
	file.close();
	// get all sheets and process them
	Stream<Sheet> sheetStream = wb.getSheets();
	HashSet<Cell[][]> tables = new HashSet<>();
	sheetStream.forEach( sheet -> {
		try { tables.addAll(splitSheet(3, 3, sheetToArray(sheet))); }
		catch (IOException e) { throw new RuntimeException(e); }
	});

	wb.close();
	return tables;
}


public Set<Cell[][]> splitSheet (int minHeight, int minWidth, Cell[][] cells)
{
	HashSet<Cell[][]> l = new HashSet<>();
	for (int i = 0; i < cells.length; i += minHeight)
	{
		for (int j = 0; j < cells[0].length; j += minWidth)
		{
			// found non-empty cell
			if (cells[i][j] != null && cells[i][j].getType() != CellType.EMPTY && !inMultiRange(i,j))
			{
				logger.info("splitSheet " + i + " " + j + " " + cells[i][j].getRawValue()); // todo: debug
				int[] cornerCellCoords = findCornerCell(cells, i, j);
				// Find everything in sub-table
				if (cornerCellCoords != null)  // we should have already skipped processed cells
					l.add(getTable(cells, cornerCellCoords[0], cornerCellCoords[1]));
			}
		}
	}
	return l;
}


private Cell[][] sheetToArray (Sheet sheet) throws IOException
{
	Stream<Row> rowStream = sheet.openStream();
	// Get table bounds
	Row[] rowArray = rowStream.toArray(Row[]::new);
	rowStream.close();

	int maxY = rowArray.length;
	int maxX = rowArray[0].getCellCount();
	for (Row cells : rowArray) {
		Cell[] cellArray = cells.stream().toArray(Cell[]::new);
		if (maxX < cellArray.length) maxX = cellArray.length;
	}

	// Array table
	Cell[][] table = new Cell[maxY][maxX];
	for (int i = 0; i < maxY; i++)
	{
		for (int j = 0; j < maxX; j++)
		{
			if (rowArray[i].hasCell(j)) table[i][j] = rowArray[i].getCell(j);
			else table[i][j] = null;
		}
	}
	return table;
}


private int[] findCornerCell (Cell[][] cells, int i, int j)
{
				logger.info("findCornerCell "+i+" "+j + " " + cells[i][j].getRawValue()); // todo: debug
	// do not run this if the cell has already been processed
	if (inMultiRange(i, j)) return null;
	// BFS Constants
	final int[] dRow = { -1, 0, 1, 0 };
	final int[] dCol = { 0, 1, 0, -1 };
	// Detected non-empty cell
	// Find top left cell of rectangle using BFS
	Queue<int[]> q = new LinkedList<>();
	int ni = i;
	int nj = j;
	q.add(new int[] {i, j});
	// Array of 'seen' cells
	boolean[][] seen = new boolean[3][3];
	seen[0][0] = true;
	// Iterate while the queue is not empty
	while (!q.isEmpty())
	{
		int[] t = q.peek();
		// check if it's a corner cell, and return
		if ((t[0] == 0 && t[1] == 0) ||
			(cells[ t[0] - 1 ][t[1]].getType() == CellType.EMPTY && cells[t[0]][ t[1] - 1 ].getType() == CellType.EMPTY))
			return t;

		// Normally the function will be on this line, but since we're
		// not doing anything, we simply proceed
		q.remove();
		// Go to the adjacent cells
		for (int a = 0; a < 4; a++)
		{
			ni += dRow[a];
			nj += dCol[a];
			// Check if NOT out of bounds AND NOT yet visited
			if (
				ni >= i - 2 && ni <= i &&
				nj >= j - 2 && nj <= j &&
				!seen[i-ni][j-nj]
			)
			{
				q.add(new int[] {ni, nj});
				seen[i-ni][j-nj] = true;
			}
		}
	}
	return null;
}


private Cell[][] getTable (final Cell[][] cells, final int startRow, final int startCol)
{
	// find bounds (y)
	int r = startRow;
	int y = 1;
	logger.info("getTable cells.length " + cells.length); // todo: debug
	while (r < cells.length && (cells[r][startCol] != null || cells[r][startCol].getType() != CellType.EMPTY))
	{
		logger.info("getTable row " + r + " " + cells[r][startCol].getRawValue()); // todo: debug
		y++;
		r++;
	}
	logger.info("getTable final y " + y); // todo: debug
	// find bounds (x)
	int c = startCol;
	int x = 1;
	logger.info("getTable cells[startRow].length " + cells[startRow].length); // todo: debug
	while (c < cells[startRow].length && (cells[startRow][c] != null || cells[startRow][c].getType() != CellType.EMPTY))
	{
		logger.info("getTable col " + c + " " + cells[startRow][c].getRawValue()); // todo: debug
		x++;
		c++;
	}
	logger.info("getTable final x " + x); // todo: debug

	// idk why but the final bounds will always be 1 too many
	y--; x--;
	Cell[][] subSheet = new Cell[y][x];
	for (int i = 0; i < y; i++) {
		for (int j = 0; j < x; j++) {
			subSheet[i][j] = cells[startRow + i][startCol + j];
		}
	}

	processedCells.add(new int[] {startRow, startRow + y, startCol, startCol + x});
	return subSheet;
}

private boolean inMultiRange(int y, int x) {
	for (int[] range : this.processedCells) 
		if (range[0] <= y && y <= range[1] && range[2] <= x && x <= range[3]) return true;
	return false;
}
}
