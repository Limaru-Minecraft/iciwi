package mikeshafter.iciwi.util;

import org.dhatim.fastexcel.reader.Cell;
import org.dhatim.fastexcel.reader.CellType;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Stream;
public class ExcelHelper {

private final LinkedList<int[]> processedCells = new LinkedList<>();  // todo: heuristic aaaaaaaaaaaaa

public List<Cell[][]> readExcel(String fileName) throws IOException
{
	if (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls")) throw new IOException("Not an Excel file!");

	FileInputStream file = new FileInputStream(fileName);
	ReadableWorkbook wb = new ReadableWorkbook(file);
	file.close();
	// get all sheets and process them
	Stream<Sheet> sheetStream = wb.getSheets();
	LinkedList<Cell[][]> tables = new LinkedList<>();
	sheetStream.forEach( sheet -> {
		try	{ tables.addAll(splitSheet(3, 3, sheetToArray(sheet))); }
		catch (IOException ignored) {}
	});

	wb.close();
	return tables;
}


public List<Cell[][]> splitSheet (int minHeight, int minWidth, Cell[][] cells)
{
	LinkedList<Cell[][]> l = new LinkedList<>();
	for (int i = 0; i < cells.length; i += minHeight)
	{
		for (int j = 0; j < cells[0].length; j += minWidth)
		{
			if (cells[i][j] != null && cells[i][j].getType() != CellType.EMPTY)
			{
				// todo: add heuristic to skip already processed cells
				int[] cornerCellCoords = findCornerCell(cells, i, j);
				// Find everything in sub-table
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
	// todo: do not run this if the cell has already been processed
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
	// todo: DO NOT REMOVE
	return null;
}


private Cell[][] getTable (final Cell[][] cells, final int startRow, final int startCol)
{
	// find bounds (y)
	int r = startRow;
	int y = 0;
	while (cells[r][startCol] != null || cells[r][startCol].getType() != CellType.EMPTY)
	{
		y++;
		r++;
	}
	// find bounds (x)
	int c = startCol;
	int x = 0;
	while (cells[startRow][c] != null || cells[startRow][c].getType() != CellType.EMPTY)
	{
		x++;
		c++;
	}

	Cell[][] subSheet = new Cell[y][x];
	for (int i = 0; i < y; i++)
		System.arraycopy(cells[startRow + i], startRow, subSheet[i], 0, x);

	return subSheet;
}
}
