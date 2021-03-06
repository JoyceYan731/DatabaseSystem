package operators;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import data.Tuple;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

/**
 * This class provides function:
 * Sort Tuples according to required sequence
 * 
 * @author Xiaoxing Yan
 *
 */
public class SortOperator extends Operator{

	
	private PlainSelect plainSelect;
	private String[] sortSequence;
	/*store all Tuples for before sorting*/
	private List<Tuple> dataCollection;
	/*record the index when using getNextTuple*/
	private int currentIndex = 0;

	/** 
	 * This method is a constructor which is to
	 * initialize related fields.
	 * 
	 * @param plainSelect  PlainSelect of query
	 * @param op  pass in child operator
	 * 
	 */
	public SortOperator(PlainSelect plainSelect, Operator op) {
		StringBuilder sb = new StringBuilder();
		sb.append("sort-");
		sb.append(op.name);
		name = sb.toString();

		this.plainSelect = plainSelect;
		setLeftChild(op);

		dataCollection = new ArrayList<Tuple>();
		Tuple current = getLeftChild().getNextTuple();

		/*get sorting sequence*/
		if (current != null) {
			schema = current.getSchema();
		} else {
			/*deal with null situation*/
			schema = new HashMap<String, Integer>();
		}
		
		Map<Integer, String> pairs = new HashMap<Integer, String>();
		for (String key : schema.keySet()) {
			pairs.put(schema.get(key), key);

		}
		sortSequence = new String[pairs.size()];
		for (int i=0; i<pairs.size(); i++) {
			sortSequence[i] = pairs.get(i);
		}

		/*read all tuples*/
		while (current!=null) {
			dataCollection.add(current);
			current = getLeftChild().getNextTuple();
		}

		Collections.sort(dataCollection, new TupleComparator());
		getLeftChild().reset();

	}

	/**
	 * This method is to get the next tuple after sorting
	 * 
	 * @return next tuple after sorting
	 */
	@Override
	public Tuple getNextTuple() {
		Tuple current = null;

		if (currentIndex < dataCollection.size()) {
			current = dataCollection.get(currentIndex);
			currentIndex++;
		}
		return current;
	}


	/**
	 * This method is to reset sort operator
	 * by resetting its currentIndex
	 */
	@Override
	public void reset() {
		currentIndex = 0;
	}



	/**
	 * This class is to customize a Comparator according to
	 * the ORDER BY
	 * 
	 * @author yanxiaoxing
	 *
	 */

	class TupleComparator implements Comparator<Tuple> {

		/*get the required sorting sequence */
		List<OrderByElement> list = plainSelect.getOrderByElements();

		@Override
		public int compare(Tuple o1, Tuple o2) {
			if (list != null) {
				for (int i=0; i<list.size(); i++) {
					Integer col = o1.getSchema().get(list.get(i).toString());
					if (o1.getData()[col] < o2.getData()[col]) {
						return -1;
					} 
					if (o1.getData()[col] > o2.getData()[col]){
						return 1;
					} 

				}



				/*
				 * Break ties in the rest of attributes.
				 * 
				 * we can directly iterate over every element
				 * because we have checked all required attributes, which means
				 * the previous comparing results must be zero
				 */

				for (int i=0; i< sortSequence.length; i++) {
					Integer col = o1.getSchema().get(sortSequence[i]);
					if (o1.getData()[col] < o2.getData()[col]) {
						return -1;
					} 
					if (o1.getData()[col] > o2.getData()[col]){
						return 1;
					} 
				}

				return 0;

			}

			/*if there is no ORDER BY in query
			 *return tuple directly without sorting
			 */
			return 0;
		}
	}
	
	
//	public static void main(String[] args) throws JSQLParserException {
//		// TODO Auto-generated method stub
//		String[] statements = new String[1];
//		
//		statements[0] = "SELECT * FROM Sailors S ORDER BY S.B;";
//		
//		
//		for (int i=0; i< statements.length; i++) {
//			System.out.println("*******when statement is : " + statements[i]);
//			String statement = statements[i];
//			CCJSqlParserManager parserManager = new CCJSqlParserManager();
//			PlainSelect ps = (PlainSelect) ((Select) parserManager.
//					parse(new StringReader(statement))).getSelectBody();
//			String table_info = ps.getFromItem().toString();
//			ScanOperator scanOp = new ScanOperator(table_info);
//			SortOperator sortOp = new SortOperator(ps, scanOp);
//			ProjectOperator projectOp = new ProjectOperator(ps, sortOp);
//			projectOp.dump();
//			System.out.println("*******end*********");
//			System.out.println();
//
//		}
//		ScanOperator sc = new ScanOperator("Boats");
//		List<String> order = new LinkedList<String>();
//		order.add("Boats.E");
//		ExternalSortOperator es = new ExternalSortOperator(1, 3, order, sc.schema, sc);
//		//es.dump();
//		for (int i = 0; i<100; i++) {
//			Tuple t = es.getNextTuple();
//			t.printData();

//	}

}