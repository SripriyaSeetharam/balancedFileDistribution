/*
--------------------------
Advanced Computing Services Programming Challenge
Distribution Plan: given set of files and compute nodes
Author: Sripriya Seetharam
--------------------------------
*/
import java.io.*;
import java.util.*;

/*The main Solution class
*/
public class Solution {
	
	/* If -o option is not given */
	private boolean console_output = true;
	
	/* output filename*/
	private String output_file_name = null;
	
	/*file writer*/
	private PrintWriter writer = null;
	
	/*list to hold all file values*/
	private List<Integer> fileSize = null;
	
	/*list to hold all node value*/
	private List<Integer> nodeSize = null;
	
	/* input file list text*/
	private String input_file_name = null;
	
	/* input node list text*/
	private String input_node_file_name = null;
	
	/* the main array list holding an array of array list each containing file values */
	private ArrayList<ArrayList<Integer>> result_array = null;
	
	/* file reader*/
	private BufferedReader bReader;
	
	//Rejection File List
	private List<Integer> rejectList = null;
	
	/* to handle nodes or files having same value*/
	private HashMap<String,Boolean> file_line_visited = new HashMap<String,Boolean>();
	
	/* the main logic for splitting the list of files based on a balancing criteria and
	mapping the files to the selected node*/
	void load_balancer() throws IOException {
		
		int y = 0;
		
		/*if any file size greater than a given node's highest capacity
		put it to reject list*/
		rejectList = new ArrayList<Integer>();
		Collections.sort(nodeSize,Collections.reverseOrder()); 
		while(y<fileSize.size()) {	
				if(fileSize.get(y)>nodeSize.get(0)) {
					rejectList.add(fileSize.get(y));
					fileSize.remove(fileSize.get(y));
				}
			y++;
		}
		
		
		int sum=0;
		for(int i=0;i<fileSize.size();i++)
			sum=sum+fileSize.get(i);

		//Find average size per container
		
		int avgPerContainer = sum/nodeSize.size();
		
		//container array containing the average file size distribution
		List<Integer> contnrArray = new ArrayList<Integer>(Collections.nCopies(nodeSize.size(), avgPerContainer));
		
		//Array of ArrayLists containing the files list in each ArrayList
		result_array = new ArrayList<ArrayList<Integer>>(nodeSize.size());
		//initialize the array of linkedList
		for (int i = 0; i < nodeSize.size(); i++) {
			result_array.add(new ArrayList<Integer>());
		}

		
		List<Integer> resultSum = new ArrayList<Integer>();
		
		List<Integer> diffList = new ArrayList<Integer>();
		

		//Sort the nodes in decreasing capacity
		
		Collections.sort(fileSize,Collections.reverseOrder()); 
		

		//Map the  delta (avgperContainer - first_file) to first container -default
		contnrArray.set(0,contnrArray.get(0)-fileSize.get(0));
		
		//Add the 1st file to result list
		result_array.get(0).add(fileSize.get(0));

		
		/*First loop Begins*/
		int idx = 0;
		int value = 0;
		for (int i = 1; i < fileSize.size(); i++) {
			idx = contnrArray.indexOf(Collections.max(contnrArray));
			sum=0;
			for(int i1=0;i1<result_array.get(idx).size();i1++)
			{
				value = (int) result_array.get(idx).get(i1);
				sum = sum + value;
			}
			
				sum = sum +fileSize.get(i);
				if(sum<= nodeSize.get(idx)){
					result_array.get(idx).add(fileSize.get(i));
					contnrArray.set(idx, contnrArray.get(idx)-fileSize.get(i));
				}else{
					rejectList.add(fileSize.get(i));
				}
		}

		/*First loop ends*/
		/*if reject list is not*/
		if(!rejectList.isEmpty()){
			int tempSum=0;
			for (int i = 0; i < result_array.size(); i++){ 
				for (int j = 0; j < result_array.get(i).size(); j++) 
					tempSum= tempSum +(Integer)result_array.get(i).get(j);

				resultSum.add(tempSum);
				tempSum=0;
			}	
			
			for (int i = 0; i < resultSum.size(); i++) 
				diffList.add(nodeSize.get(i)-resultSum.get(i));
			
		
		  int tempIdx = 0;
		  boolean tempFlag = false;
		  ArrayList<Integer> rejected_indices = new ArrayList<Integer>();
		  /* map the rejected files to any of the nodes in case the the file fits in*/
		  for (int i = 0; i < rejectList.size(); i++) {
			  for (int j = 0; j < diffList.size(); j++) {
				if(rejectList.get(i)<diffList.get(j))
				{	
					tempIdx = j;
					tempFlag=true;
				}
			  }
			  if(tempFlag){
				diffList.set(tempIdx, diffList.get(tempIdx)-rejectList.get(i));
				result_array.get(tempIdx).add(rejectList.get(i));
				rejected_indices.add(rejectList.get(i));
			  }
			}
		  int i = 0;
		  while(i<rejected_indices.size()) {
			  rejectList.remove(rejected_indices.get(i));
			  i++;
		  }
		  		
		}
		create_output_distribution();

	}
	
	/* print the file name->node name format based on the result and reject list*/
	void create_output_distribution() throws IOException {
		int i = 0;
		int j = 0;
		if(!console_output) {
			BufferedWriter buf = new BufferedWriter(new FileWriter(output_file_name));
			writer = new PrintWriter(buf);
		}
		while(i<nodeSize.size()){
			String node_name = search_name(Integer.toString(nodeSize.get(i)),input_node_file_name);
			
			while(j<result_array.get(i).size()) {
				int file_size = (int)result_array.get(i).get(j);
				String file_name =search_name(Integer.toString(file_size),input_file_name);
				if(console_output)
					System.out.println(file_name+" "+node_name);
				else
					writer.println(file_name+" "+node_name);
				j++;
			}
			i++;
			j = 0;
		}
		if(writer!=null) {
			writer.close();
		}
		if(!rejectList.isEmpty()) {
			if(!console_output) {
				writer = new PrintWriter(new BufferedWriter(new FileWriter(output_file_name,true)));
			}
			
			i = 0;
			while(i<rejectList.size()) {
				int file_size = rejectList.get(i);
				
				String file_name =search_name(Integer.toString(file_size),input_file_name);
				if(console_output)
					System.out.println(file_name+" "+"NULL");
				else {
					writer.println(file_name+" "+"NULL");
				}
				i++;
			}
			if(writer!=null) {
				writer.close();
			}
		}
		bReader = new BufferedReader(new FileReader(input_file_name));
		String line=bReader.readLine();
		if(!console_output) {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(output_file_name,true)));
		}
		 while (line != null) {
			 
				  if((!(line.contains("#")) && (line.trim().length() !=0)) && file_line_visited.get(line)==false) {
					  String[] tokens = line.split("\\s");
					  if(console_output)
							System.out.println(tokens[0]+" "+"NULL");
						else {
							writer.println(tokens[0]+" "+"NULL");
						}
				  }
				  line = bReader.readLine();
		 }
		 if(bReader!=null) {
			 bReader.close();
		 }
		 
		 if(writer!=null) {
			 writer.close();
		 }
		
	}
	
	/* given the file size or node size retrieve the name */
	String search_name(String file_size, String fileName) throws IOException {
		bReader = new BufferedReader(new FileReader(fileName));
		String line=bReader.readLine();
		 while (line != null) {
				  String[] tokens = line.split("\\s");
				  if(tokens[1].equals(file_size) && (file_line_visited.get(line)==false)) {
					  file_line_visited.put(line,true);
					  bReader.close();
					  return tokens[0];
				  }
			      line = bReader.readLine();
			}
		 if(bReader!=null)
			 bReader.close();
		 return null;
	}
	
	/* handle all the input command lien arguments*/
	int input_files_read(String[] input_files) throws FileNotFoundException, IOException
	{	
		boolean file_input = false;
		boolean node_input = false;
		for(int i = 0;i<input_files.length;i++) {
			switch(input_files[i]) {
			case "-h":
				System.out.println("Usage: java Solution [-options]"+"\n"+
				"where options include:"+"\n"+
				    "-f filename: Input file for files, e.g. -f files.txt. Required."+"\n"+
				    "-n filename: Input file for nodes, e.g. -n nodes.txt. Required."+"\n"+
				    "-o filename: Output file, e.g. -o result.txt.If this option is not given,standard output - console"+"\n"+
				    "-h: Display usage information");
				return 0;
			case "-f":
				i++;
				if(input_files[i] != null) {
					fileSize = new ArrayList<Integer>();
					input_file_name = input_files[i];
					file_input = file_read(input_file_name,fileSize);
				}
				else {
					System.out.println("Input file name is missing");
					return 0;
				}
				break;
			case "-n":
				i++;
				if(input_files[i] != null) {
					nodeSize = new ArrayList<Integer>();
					input_node_file_name = input_files[i];
					node_input = file_read(input_node_file_name,nodeSize);
				}
				else {
					System.out.println("Input file name is missing");
					return 0;
				}
				break;
			case "-o":
				i++;
				if(input_files[i] != null) {	
					output_file_name = input_files[i];
					console_output = false;
				}
				else {
					System.out.println("Ouput file name is missing");
					return 0;
				}
				break;
			default:
				System.out.println("Unknown options: Type -h for more information");
				return 0;
			}
		}
		if(file_input && node_input)
				return 1;
		else
			return 0;
		
	}
	
	/* read the key pair values from files and nodes txt and add them to the file or the node list*/
	boolean file_read(String fileName, List<Integer> inputSize) throws FileNotFoundException,IOException {  
		 bReader = new BufferedReader(new FileReader(fileName));
		 String line=bReader.readLine();
		 while (line != null) {
			  if (!(line.contains("#")) && (line.trim().length() !=0)) {
				  String[] tokens = line.split("\\s");
				  if(tokens != null &&(tokens.length==2)) {
				  	  try {
				  	  int value = Integer.parseInt(tokens[1]);
				  	  if(value>0)
				  	  	inputSize.add(Integer.parseInt(tokens[1]));
					  } catch(NumberFormatException e) {
					  	e.toString();
					  }				  
					  file_line_visited.put(line,false);
				  } else {
					  System.out.println("Input file - incorrect format");
					  return false;
				  }
			  }
			  line = bReader.readLine();
			}
		 bReader.close();
		 return true;
	}

	/**
	 * @param args
	 *
	 */
	public static void main(String[] args){
		Solution load_balancer_solution = new Solution();
		
		int valid = 0;
		if(args.length!=0)
			try {
				valid = load_balancer_solution.input_files_read(args);
			} catch (IOException e) {
				System.out.println("Input file not found!");
			}
		else {
			System.out.println("Type -h for usage information");
			return;
		}
		
		if(valid==1)
			try {
				load_balancer_solution.load_balancer();
			} catch (IOException e) {
				System.out.println("Error reading file!");
			}
		else {
			System.out.println("Type -h for correct usage information");
		}
	}
	
}
