package edu.uci.cs241.compiler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class compiler {
	// //////array
	// //////read write
	public static void main(String[] args) throws IOException {
		compiler c = new compiler("D:/compiler/xxx.txt");
		// System.out.print(c.in);
		c.computation();
		compiler.test("D:/compiler/front.vcg");
		c.testDomTree("D:/compiler/3.vcg");
		c.CSE();
		c.makeDUEChain();
		c.testDUChain();
		// c.testDomTree("D:/compiler/dom.vcg");

		// //////!!!! test must be here to add succ and pre between nodes
		compiler.test("D:/compiler/org.vcg");
		c.testPreSucc();
		c.RA();
		compiler.test("D:/compiler/ra.vcg");
		testColor("D:/compiler/color.vcg");

		int[] x = c.generateCode();

		DLX.load(x);
		// System.out.println("xleng "+x.length);
		DLX.execute();
		// c.testGCArray(x);
		// c.testDUChain();
		// c.testDomTree("D:/compiler/3.vcg");
	}

	public int[] generateCode() {
		int[] result = new int[2498];

		int count = 0;
		for (Node n : SSANodes) {
			for (int li : n.label) {
				GCLineIndex.put(li, count);
				line l = lines.get(li);

				if (l.op.equals("spStore")) {
					if (l.sink.charAt(0) == '^') {
						l.sink = l.opr1;
						l.opr1 = "0";
						spillStack.add(count);
						result[count] = this.makeGCLine(l, 36, 1, 2);
						count++;
					} else {
						l.opr1 = "0";
						spillStack.add(count);
						result[count] = this.makeGCLine(l, 36, 1, 2);
						count++;
					}
				} else if (l.op.equals("spLoad")) {
					if (l.opr2.charAt(0) == '#') {
						// System.out.println(l.sink+"
						// "+l.opr2+"!!@!@!"+l.opr1);
						l.opr1 = "0";
						spillStack.add(count);
						result[count] = this.makeGCLine(l, 32, 1, 2);
						count++;
					} else {
						l.opr1 = "0";
						spillStack.add(count);
						result[count] = this.makeGCLine(l, 32, 1, 0);
						count++;
					}
				} else if (l.op.equals("retAddr")) {
					retStack.add(count);
					result[count] = this.makeGCLine(l, 16, 1, 2);
					count++;
				} else if (l.op.equals("skStore")) {

					if (l.opr2.charAt(0) == '!') {
						l.opr2 = "0";
						FPStack.add(count);
						result[count] = this.makeGCLine(l, 36, 1, 0);
						count++;
						continue;
					}
					if (l.opr2.charAt(0) == '#') {
						slStack.add(count);
						result[count] = this.makeGCLine(l, 36, 1, 2);
						count++;
					} else {
						slStack.add(count);
						result[count] = this.makeGCLine(l, 36, 1, 0);
						count++;
					}

				} else if (l.op.equals("skLoad")) {

					if (l.opr2.charAt(0) == '!') {
						l.opr2 = "0";
						FPStack.add(count);
						result[count] = this.makeGCLine(l, 32, 1, 0);
						count++;
						continue;
					}
					if (l.opr2.charAt(0) == '#') {
						slStack.add(count);
						result[count] = this.makeGCLine(l, 32, 1, 2);
						count++;
					} else {
						slStack.add(count);
						result[count] = this.makeGCLine(l, 32, 1, 0);
						count++;
					}
				} else if (l.op.equals("arrLoad")) {
					arrStack.add(count);
					result[count] = this.makeGCLine(l, 32, 1, 0);
					count++;
				} else if (l.op.equals("arrStore")) {
					arrStack.add(count);
					result[count] = this.makeGCLine(l, 36, 1, 0);
					count++;
				} else if (l.op.equals("gStore")) {
					globalVarStack.add(count);
					result[count] = this.makeGCLine(l, 36, 1, 2);
					count++;
				} else if (l.op.equals("gLoad")) {
					globalVarStack.add(count);
					result[count] = this.makeGCLine(l, 32, 1, 2);
					count++;
				} else if (l.op.equals("add")) {
					if (l.opr1.charAt(0) == '#' && l.opr2.charAt(0) == '#') {
						int x = Integer.valueOf(l.opr1.substring(1));
						int y = Integer.valueOf(l.opr2.substring(1));
						l.opr2 = "#" + (x + y);
						l.opr1 = "0";
						result[count] = this.makeGCLine(l, 16, 1, 2);
						count++;
					} else if (l.opr1.charAt(0) == '#') {
						result[count] = this.makeGCLine(l, 16, 1, 11);
						count++;
					} else if (l.opr2.charAt(0) == '#') {
						result[count] = this.makeGCLine(l, 16, 1, 2);
						count++;
					} else {
						result[count] = this.makeGCLine(l, 0, 2, 0);
						count++;
					}
				} else if (l.op.equals("adda")) {
					if (l.opr1.charAt(0) == '#' && l.opr2.charAt(0) == '#') {
						int x = Integer.valueOf(l.opr1.substring(1));
						int y = Integer.valueOf(l.opr2.substring(1));
						l.opr2 = "#" + (x + y);
						l.opr1 = "0";
						result[count] = this.makeGCLine(l, 16, 1, 2);
						count++;
					} else if (l.opr1.charAt(0) == '#') {
						result[count] = this.makeGCLine(l, 16, 1, 11);
						count++;
					} else if (l.opr2.charAt(0) == '#') {
						result[count] = this.makeGCLine(l, 16, 1, 2);
						count++;
					} else {
						result[count] = this.makeGCLine(l, 0, 2, 0);
						count++;
					}
				} else if (l.op.equals("mul")) {
					if (l.opr1.charAt(0) == '#' && l.opr2.charAt(0) == '#') {
						int x = Integer.valueOf(l.opr1.substring(1));
						int y = Integer.valueOf(l.opr2.substring(1));
						l.opr2 = "#" + (x * y);
						l.opr1 = "0";
						result[count] = this.makeGCLine(l, 18, 1, 2);
						count++;
					} else if (l.opr1.charAt(0) == '#') {
						result[count] = this.makeGCLine(l, 18, 1, 11);
						count++;
					} else if (l.opr2.charAt(0) == '#') {
						result[count] = this.makeGCLine(l, 18, 1, 2);
						count++;
					} else {
						result[count] = this.makeGCLine(l, 2, 2, 0);
						count++;
					}
				} else if (l.op.equals("mula")) {
					if (l.opr1.charAt(0) == '#' && l.opr2.charAt(0) == '#') {
						int x = Integer.valueOf(l.opr1.substring(1));
						int y = Integer.valueOf(l.opr2.substring(1));
						l.opr2 = "#" + (x * y);
						l.opr1 = "0";
						result[count] = this.makeGCLine(l, 18, 1, 2);
						count++;
					} else if (l.opr1.charAt(0) == '#') {
						result[count] = this.makeGCLine(l, 18, 1, 11);
						count++;
					} else if (l.opr2.charAt(0) == '#') {
						result[count] = this.makeGCLine(l, 18, 1, 2);
						count++;
					} else {
						result[count] = this.makeGCLine(l, 2, 2, 0);
						count++;
					}
				} else if (l.op.equals("sub")) {
					if (l.opr1.charAt(0) == '#' && l.opr2.charAt(0) == '#') {
						int x = Integer.valueOf(l.opr1.substring(1));
						int y = Integer.valueOf(l.opr2.substring(1));
						l.opr2 = "#" + (x - y);
						l.opr1 = "0";
						result[count] = this.makeGCLine(l, 17, 1, 2);
						count++;
					} else if (l.opr1.charAt(0) == '#') {
						result[count] = this.makeGCLine(l, 17, 1, 11);
						count++;
					} else if (l.opr2.charAt(0) == '#') {
						result[count] = this.makeGCLine(l, 17, 1, 2);
						count++;
					} else {
						result[count] = this.makeGCLine(l, 1, 2, 0);
						count++;
					}
				} else if (l.op.equals("div")) {
					if (l.opr1.charAt(0) == '#' && l.opr2.charAt(0) == '#') {
						int x = Integer.valueOf(l.opr1.substring(1));
						int y = Integer.valueOf(l.opr2.substring(1));
						int z = x / y;
						l.opr2 = "#" + z;
						l.opr1 = "0";
						result[count] = this.makeGCLine(l, 19, 1, 2);
						count++;
					} else if (l.opr1.charAt(0) == '#') {
						result[count] = this.makeGCLine(l, 19, 1, 11);
						count++;
					} else if (l.opr2.charAt(0) == '#') {
						result[count] = this.makeGCLine(l, 19, 1, 2);
						count++;
					} else {
						result[count] = this.makeGCLine(l, 3, 2, 0);
						count++;
					}
				} else if (l.op.equals("mov")) {
					l.opr2 = "0";
					if (l.opr1.charAt(0) == '#') {
						l.opr2 = l.opr1;
						l.opr1 = "0";
						result[count] = this.makeGCLine(l, 16, 1, 2);
						count++;
					} else {
						result[count] = this.makeGCLine(l, 16, 1, 0);
						count++;
					}

				} else if (l.op.equals("read")) {
					l.op = "RDI";
					l.opr1 = "0";
					l.opr2 = "0";
					result[count] = this.makeGCLine(l, 50, 2, 0);
					count++;

				} else if (l.op.equals("write")) {
					l.opr2 = "0";
					if (l.opr1.charAt(0) == '#') {
						printError("gcWrite");
					} else {
						result[count] = this.makeGCLine(l, 51, 2, 0);
						count++;
					}

				} else if (l.op.equals("wnl")) {
					l.sink = "0";
					l.opr1 = "0";
					l.opr2 = "0";
					result[count] = this.makeGCLine(l, 53, 1, 0);
					count++;
				} else if (l.op.equals("read")) {
					result[count] = this.makeGCLine(l, 50, 2, 0);
					count++;
				} else if (l.op.equals("bne")) {
					l.sink = l.opr1;
					l.opr1 = "#0";
					result[count] = this.makeGCLine(l, 41, 1, 1);
					branchStack.add(count);
					count++;
				} else if (l.op.equals("bgt")) {
					l.sink = l.opr1;
					l.opr1 = "#0";
					result[count] = this.makeGCLine(l, 45, 1, 1);
					branchStack.add(count);
					count++;
				} else if (l.op.equals("beq")) {
					l.sink = l.opr1;
					l.opr1 = "#0";
					result[count] = this.makeGCLine(l, 40, 1, 1);
					branchStack.add(count);
					count++;
				} else if (l.op.equals("ble")) {
					l.sink = l.opr1;
					l.opr1 = "#0";
					result[count] = this.makeGCLine(l, 44, 1, 1);
					branchStack.add(count);
					count++;
				} else if (l.op.equals("blt")) {
					l.sink = l.opr1;
					l.opr1 = "#0";
					result[count] = this.makeGCLine(l, 42, 1, 1);
					branchStack.add(count);
					count++;
				} else if (l.op.equals("bge")) {
					l.sink = l.opr1;
					l.opr1 = "#0";
					result[count] = this.makeGCLine(l, 43, 1, 1);
					branchStack.add(count);
					count++;
				} else if (l.op.equals("bsr")) {
					l.opr1 = "#0";
					result[count] = this.makeGCLine(l, 46, 1, 1);
					branchStack.add(count);
					count++;
				} else if (l.op.equals("return")) {
					if (l.opr1 != null && l.opr1.charAt(0) == '&') {
						l.opr1 = "0";
						result[count] = this.makeGCLine(l, 49, 2, 0);
						count++;
					} else {
						l.opr1 = "0";
						result[count] = this.makeGCLine(l, 49, 2, 0);
						count++;
					}

				} else if (l.op.equals("cmp")) {
					if (l.opr1.charAt(0) == '#') {
						printError("gccmp");
					}
					if (l.opr2.charAt(0) == '#') {
						result[count] = this.makeGCLine(l, 21, 1, 2);
						count++;
					} else {
						result[count] = this.makeGCLine(l, 5, 2, 0);
						count++;
					}
				}
			}
		}
		for (int i = count; i < result.length; i++) {
			result[i] = 0;
		}

		return this.patchBrach(count, result);
	}

	public void testGCArray(int[] x) {
		// System.out.println("________________");
		// for(int i = 0; i <x.length;i++)
		// {
		// System.out.println(i+" : "+Integer.toBinaryString(x[i]));
		// }
		// System.out.println("________________");
	}

	public int[] patchBrach(int size, int[] mashCode) {
		// System.out.println("++++++++++++++++");
		int arrBase = size;
		int spillBase = arrBase + arrayStackTop;
		int globalVarBase = spillBase + spillStackTop;
		int stackBase = globalVarBase + globalStackTop + 1;
		// System.out.println("arrBase"+arrBase);
		// System.out.println("spillBase"+spillBase);
		// System.out.println("globalVarBase"+globalVarBase);
		// System.out.println("stackBase"+stackBase);

		for (int i : branchStack) {
			int temp = mashCode[i];
			int x = temp >> 16;
			int y = x << 16;
			// System.out.println(SSANodes.size()+"%%"+(mashCode[i] - y));
			Node n = SSANodes.get(mashCode[i] - y);
			int brIx = n.label.get(0);

			if (GCLineIndex.containsKey(brIx)) {
				int count = GCLineIndex.get(brIx) - i;
				if (count < 0) {
					x = count << 16;
					count = x >>> 16;
				}
				mashCode[i] = y + count;
				// System.out.println(i+" :
				// "+Integer.toBinaryString(mashCode[i]));
			} else
				printError("patchBack1");
		}
		for (int i : retStack) {
			int temp = mashCode[i];
			int x = temp >> 16;
			int y = x << 16;
			int c = SSANodes.get(mashCode[i] - y).label.get(0);
			if (GCLineIndex.containsKey(c)) {
				int count = GCLineIndex.get(c) * 4;
				if (count < 0) {
					x = count << 16;
					count = x >>> 16;
				}
				mashCode[i] = y + count;
			} else
				printError("patchBack1");
		}
		for (int i : arrStack) {
			int temp = mashCode[i];
			int x = temp >> 16;
			int y = x << 16;
			int c = arrBase + mashCode[i] - y;
			mashCode[i] = y + c * 4;

		}
		for (int i : FPStack) {
			int temp = mashCode[i];
			int x = temp >> 16;
			int y = x << 16;
			int c = stackBase - 1 + mashCode[i] - y;
			mashCode[i] = y + c * 4;
		}
		for (int i : spillStack) {
			int temp = mashCode[i];
			int x = temp >> 16;
			int y = x << 16;
			int c = spillBase + mashCode[i] - y;
			mashCode[i] = y + c * 4;

		}
		for (int i : globalVarStack) {
			int temp = mashCode[i];
			int x = temp >> 16;
			int y = x << 16;
			int c = globalVarBase + mashCode[i] - y;
			mashCode[i] = y + c * 4;
		}
		for (int i : slStack) {
			int temp = mashCode[i];
			int high = (temp >> 16) << 16;

			int x = temp << 16;
			int y = x >> 16;
			int z = y / 4;
			int c = stackBase + z;
			mashCode[i] = high + c * 4;
		}
		return mashCode;
	}

	public int getIntLn(String s) {
		if (s == null)
			return 0;
		return Integer.valueOf(s);
	}

	public int makeGCLine(line l, int op, int type, int pos) {
		int a = this.getIntLn(l.sink);
		int b = 0;
		int c = 0;
		if (pos == 0)// no const
		{
			b = this.getIntLn(l.opr1);
			c = this.getIntLn(l.opr2);

		} else if (pos == 1)// const in b
		{
			b = Integer.valueOf(l.opr1.substring(1, l.opr1.length()));
			c = this.getIntLn(l.opr2);
		} else if (pos == 11) {
			c = Integer.valueOf(l.opr1.substring(1, l.opr1.length()));
			b = this.getIntLn(l.opr2);
		} else if (pos == 2)// const in c
		{
			b = this.getIntLn(l.opr1);
			c = Integer.valueOf(l.opr2.substring(1, l.opr2.length()));
		} else if (pos == 3)// const in b & null in c
		{
			b = Integer.valueOf(l.opr1.substring(1, l.opr1.length()));
		} else if (pos == 4)// const in c & null in b
		{
			c = Integer.valueOf(l.opr2.substring(1, l.opr2.length()));
		} else if (pos == 5)// const in b & c
		{
			b = Integer.valueOf(l.opr1.substring(1, l.opr1.length()));
			c = Integer.valueOf(l.opr2.substring(1, l.opr2.length()));
		} else {
			printError("makeGCline");
		}

		if (type == 1) {
			return makeTupleF1(op, a, b, c);
		} else if (type == 2) {
			return makeTupleF2(op, a, b, c);
		} else if (type == 3) {
			return makeTupleF3(op, c);
		} else {
			printError("makeGCline1");
		}
		return -1;
	}

	public static int makeTupleF1(int op, int a, int b, int c) {
		int x = 0;
		if (a < 0) {
			x = a << 27;
			a = x >>> 27;
		}
		if (b < 0) {
			x = b << 27;
			b = x >>> 27;
		}
		if (c < 0) {
			x = c << 16;
			c = x >>> 16;
		}
		op = op << 5;
		op += a;
		op = op << 5;
		op += b;
		op = op << 16;
		op += c;
		return op;
	}

	public static int makeTupleF2(int op, int a, int b, int c) {
		int x;
		if (a < 0) {
			x = a << 27;
			a = x >>> 27;
		}
		if (b < 0) {
			x = b << 27;
			b = x >>> 27;
		}
		if (c < 0) {
			x = c << 16;
			c = x >>> 16;
		}
		op = op << 5;
		op += a;
		op = op << 5;
		op += b;
		op = op << 16;
		op += c;
		return op;
	}

	public static int makeTupleF3(int op, int c) {
		int x;
		if (c < 0) {
			x = c << 16;
			c = x >>> 16;
		}
		op = op << 26;
		op += c;
		return op;
	}

	public static int[] changeType(char[] p) {
		int[] r = new int[p.length];
		for (int i = 0; i < p.length; i++) {
			r[i] = p[i];
		}
		return r;
	}

	public static String readFileAsString(String filePath)
			throws java.io.IOException {
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		reader.close();
		return fileData.toString();
	}

	public compiler(String fileName) throws IOException {
		this.in = readFileAsString(fileName);

	}

	public void makeDUEChain() {
		compiler.useChain = new HashMap<String, ArrayList<Integer>>();
		compiler.defChain = new ArrayList<String>();
		compiler.defChainLn = new ArrayList<Integer>();
		compiler.PhiLines = new ArrayList<Integer>();
		for (Node n : SSANodes) {
			for (int i : n.label) {
				line l = lines.get(i);
				// System.out.println("!!!@"+i+" "+l.sink+" "+l.op+" "+l.opr1+"
				// "+l.opr2);
				if (l.opr1 != null && l.opr1.length() > 0
						&& l.opr1.charAt(0) == '(')
					addUseChain(l.opr1, i);
				if (l.opr2 != null && l.opr2.length() > 0
						&& l.opr2.charAt(0) == '(')
					addUseChain(l.opr2, i);
				if (stangeSet.contains(l.op)) {
					if (l.sink != null && l.sink.length() > 0
							&& l.sink.charAt(0) == '(')
						addUseChain(l.sink, i);
				}
				if (l.sink != null && l.sink.length() > 0
						&& l.sink.charAt(0) == '(')
					addDefChain(l.sink, i);
				if (l.op.equals("Phi"))
					PhiLines.add(i);
			}
		}
	}

	public void testDUChain() {
		// System.out.println("useChain");
		// for(String x : useChain.keySet())
		// {
		// System.out.print(x+": { ");
		// for(Integer i : useChain.get(x))
		// {
		// System.out.print(i+", ");
		// }
		// System.out.println(" } ");
		// }
		// System.out.println("defChain");
		// for(String i : defChain)
		// {
		// System.out.print(i + ", ");
		// }
	}

	public void testDomTree(String fileName) {
		String outS = "graph: {title:"
				+ '"'
				+ "SSA"
				+ '"'
				+ "\nlayoutalgorithm: dfs\nmanhattan_edges: yes\nsmanhattan_edges: yes\n";
		outS += this.testDomTreeFun(SSANodes.get(0));
		//
		outS += compiler.extendEdges + "\n";
		outS += "}\n";
		try {
			// Create file
			FileWriter fstream = new FileWriter(fileName);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(outS);
			// Close the output stream
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	public String testDomTreeFun(Node n) {
		String outS = "";
		if (n == null)
			return outS;
		outS += "node:{title: " + '"' + n.title + '"' + " \nlabel:" + '"'
				+ "\n";
		outS += "[" + n.title + "] dom [" + n.domF + "] to [" + n.next + "] \n"
				+ n.printLabel() + '"' + "}\n";
		// System.out.println("next to "+SSANodes.get(n.next).title+"}");
		// if(!n.title.equals(SSANodes.get(n.next).title) )
		if (n.children.size() == 0) {
			// System.out.println("@@"+n.title);
			if (n.title.equals(SSANodes.get(n.next).title))
				return outS;
			// this.testDomTreeFun(SSANodes.get(n.next));
			return outS;
		}

		for (Node c : n.children) {
			// System.out.println("dom:: node: "+n.title+" child: "+c.title);
			outS += "edge: {sourcename: " + '"' + n.title + '"'
					+ "\ntargetname:" + '"' +
					// n.next
					c.title + '"' + "\ncolor: red}\n";
			outS += this.testDomTreeFun(c);
		}
		return outS;
	}

	public static void testColor(String fileName) {
		// String last = "0";
		String outS = "graph: {title:"
				+ '"'
				+ "color"
				+ '"'
				+ "\nlayoutalgorithm: dfs\nmanhattan_edges: yes\nsmanhattan_edges: yes\n";
		for (vertex v : ifg.V) {
			// System.out.print(v.name+", c: "+(v.color+1)+" n:{");
			outS += "node:{title: " + '"' + v.name + '"' + " \nlabel:" + '"';
			outS += v.name + "  color :" + v.color + " \n " + " \n"
					+ v.printNeighbor() + '"' + "}\n";
			for (vertex nv : v.neighbor) {
				// System.out.print("["+nv.name+"], ");
				outS += "edge: {sourcename: " + '"' + v.name + '"'
						+ "\ntargetname:" + '"' + nv.name + '"'
						+ "\ncolor: blue}\n";
			}
			// System.out.println("}");
		}
		outS += "}\n";

		try {
			// Create file
			FileWriter fstream = new FileWriter(fileName);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(outS);
			// Close the output stream
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	public void addReturn() {

		for (Node n : SSANodes) {
			if (n.succ.size() == 0) {
				n.label.add(compiler.curLine++);
				line l = new line(null, "return", null, "0");
				compiler.lines.add(l);
				return;
			}
		}
		printError("addReturn");
	}

	public void addBSR() {
		for (Node n : SSANodes) {
			if (!n.title.equals(SSANodes.get(n.next).title)) {
				this.addBsr(n, SSANodes.get(n.next));
			}
		}
	}

	public void testPreSucc() {
		// for(Node n: SSANodes)
		// {
		// System.out.print(n.title+" pre{ ");
		// for(Node p : n.pre)
		// System.out.print(p.title+",");
		// System.out.print("} succ{");
		// for(Node s : n.succ)
		// System.out.print(s.title+", ");
		// System.out.println("}");
		// }
	}

	public static String printSucc(Node n) {
		String s = "";
		for (Node x : n.succ) {
			s += x.title;
			s += ", ";
		}
		return s;
	}

	public static String printPre(Node n) {
		String s = "";
		for (Node x : n.pre) {
			s += x.title;
			s += ", ";
		}
		return s;
	}

	public static void test(String fileName) {
		// String last = "0";
		String outS = "graph: {title:"
				+ '"'
				+ "SSA"
				+ '"'
				+ "\nlayoutalgorithm: dfs\nmanhattan_edges: yes\nsmanhattan_edges: yes\n";
		for (Node n : SSANodes) {
			String tempPnt;
			if (n.parent == null)
				tempPnt = null;
			else
				tempPnt = n.parent.title;
			outS += "node:{title: " + '"' + n.title + '"' + " \nlabel:" + '"'
					+ "\n";
			outS += "[" + n.title + "] pnt " + tempPnt + " \n dom " + n.domF
					+ " br " + n.branchNum + " f " + n.nodeFun + " \n" + "suc"
					+ compiler.printSucc(n) + "\n pre " + compiler.printPre(n)
					+ "\n" + n.printLabel() + '"' + "}\n";
			// System.out.println("next to "+SSANodes.get(n.next).title+"}");
			if (!n.title.equals(SSANodes.get(n.next).title)) {
				outS += "edge: {sourcename: " + '"' + n.title + '"'
						+ "\ntargetname:" + '"' +
						// n.next
						SSANodes.get(n.next).title + '"' + "\ncolor: blue}\n";
				if (!n.succ.contains(SSANodes.get(n.next)))
					n.succ.add(SSANodes.get(n.next));
				if (!SSANodes.get(n.next).pre.contains(n))
					SSANodes.get(n.next).pre.add(n);
			}
		}
		outS += compiler.extendEdges + "\n";
		outS += "}\n";

		try {
			// Create file
			FileWriter fstream = new FileWriter(fileName);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(outS);
			// Close the output stream
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	public void skim() {
		while (in.charAt(pos) == ' ' || in.charAt(pos) == '\n') {
			pos++;
		}
		return;
	}

	public void CSE() {
		for (Node n : domRoots) {
			// System.out.println("@@"+n.title);
			this.CSEFun(n);
		}
	}

	public void CSEFun(Node n) {

		for (int i = 0; i < n.label.size(); i++) {

			int ln = n.label.get(i);
			// System.out.println("!##"+i+" "+ln);
			n.repUT(ln);
			String r = n.isInQT(ln);
			if (r != null) {
				n.upByQT(ln, r);

				i--;
			} else {
				n.addQT(ln);
			}
		}
		// System.out.print(n.title+" child");
		for (Node c : n.children) {
			// System.out.println(", "+c.title);
			c.QT = n.QT;
			c.UT = n.UT;
			this.CSEFun(c);
		}
	}

	// public String advance() if you want to cancel blank, assign blank true
	public String advance() {
		int temp = in.charAt(pos);
		if (temp <= 32) {
			pos++;
			return this.advance();
		}

		else if (in.charAt(pos) == ';' || in.charAt(pos) == '('
				|| in.charAt(pos) == ')' || in.charAt(pos) == '['
				|| in.charAt(pos) == ']' || in.charAt(pos) == '{'
				|| in.charAt(pos) == '}' || in.charAt(pos) == '*'
				|| in.charAt(pos) == '+' || in.charAt(pos) == '/'
				|| in.charAt(pos) == '-' || in.charAt(pos) == '.'
				|| in.charAt(pos) == ';' || in.charAt(pos) == ',')
			return String.valueOf(in.charAt(pos++));
		else if (in.charAt(pos) == '=') {
			if (in.charAt(pos + 1) == '=') {
				pos += 2;
				return in.substring(pos - 2, pos);
			} else
				return String.valueOf(in.charAt(pos++));
		} else if (in.charAt(pos) == '!') {
			if (in.charAt(pos + 1) == '=') {
				pos += 2;
				return in.substring(pos - 2, pos);
			} else
				printError("advace1");
		} else if (in.charAt(pos) == '<') {
			if (in.charAt(pos + 1) == '=' || in.charAt(pos + 1) == '-') {
				pos += 2;
				return in.substring(pos - 2, pos);
			} else
				return String.valueOf(in.charAt(pos++));
		} else if (in.charAt(pos) == '>') {
			if (in.charAt(pos + 1) == '=') {
				pos += 2;
				return in.substring(pos - 2, pos);
			} else
				return String.valueOf(in.charAt(pos++));
		} else if (this.isNumChar(in.charAt(pos))) {
			return String.valueOf(in.charAt(pos++));
		} else {
			printError("advace");
		}
		return null;

	}

	public String nextChar() {
		return String.valueOf(in.charAt(pos));
	}

	public String next() {
		int temp = in.charAt(pos);
		if (temp <= 32) {
			pos++;
			return this.next();
		} else if (in.charAt(pos) == ';' || in.charAt(pos) == '('
				|| in.charAt(pos) == ')' || in.charAt(pos) == '['
				|| in.charAt(pos) == ']' || in.charAt(pos) == '{'
				|| in.charAt(pos) == '}' || in.charAt(pos) == '*'
				|| in.charAt(pos) == '+' || in.charAt(pos) == '/'
				|| in.charAt(pos) == '-' || in.charAt(pos) == '.'
				|| in.charAt(pos) == ';' || in.charAt(pos) == ',')
			return String.valueOf(in.charAt(pos));
		else if (in.charAt(pos) == '=') {
			if (in.charAt(pos + 1) == '=') {
				return in.substring(pos, pos + 2);
			} else
				return String.valueOf(in.charAt(pos));
		} else if (in.charAt(pos) == '!') {
			if (in.charAt(pos + 1) == '=') {
				return in.substring(pos, pos + 2);
			} else
				printError("advace1");
		} else if (in.charAt(pos) == '<') {
			if (in.charAt(pos + 1) == '=' || in.charAt(pos + 1) == '-') {
				return in.substring(pos, pos + 2);
			} else
				return String.valueOf(in.charAt(pos));
		} else if (in.charAt(pos) == '>') {
			if (in.charAt(pos + 1) == '=') {
				return in.substring(pos, pos + 2);
			} else
				return String.valueOf(in.charAt(pos));
		} else if (this.isNumChar(in.charAt(pos))) {
			int i = pos + 1;
			while (this.isNumChar(in.charAt(i))) {
				i++;
			}
			String s = in.substring(pos, i);

			return s;
		} else {
			printError("next");
		}
		return null;

	}

	public boolean isNumChar(char c) {
		if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z')
				|| (c >= 'A' && c <= 'Z'))
			return true;
		else
			return false;
	}

	public static void printError(String s) {
		// System.out.println("error: "+s);
	}

	public char letter() {
		String c = this.advance();
		if ((c.charAt(0) >= 'a' && c.charAt(0) <= 'z')
				|| (c.charAt(0) >= 'A' && c.charAt(0) <= 'Z'))
			return c.charAt(0);
		else {
			printError(this.pos + " wrong letter " + this.in.charAt(pos - 1)
					+ this.in.charAt(pos) + this.in.charAt(pos + 1));
			return 0;
		}
	}

	public int digit() {
		String c = this.advance();
		if (c.charAt(0) >= '0' && c.charAt(0) <= '9')
			return Integer.valueOf(c);
		else {
			printError("wrong digit");
			return -1;
		}
	}

	public String ident() {
		String var = String.valueOf(letter());

		while (this.isNumChar(in.charAt(pos))) {
			if ((this.next().charAt(0) >= '0' && this.next().charAt(0) <= '9'))
				var += String.valueOf(digit());
			else if ((this.next().charAt(0) >= 'a' && this.next().charAt(0) <= 'z')
					|| (this.next().charAt(0) >= 'A' && this.next().charAt(0) <= 'Z'))
				var += String.valueOf(letter());
			else
				printError("ident");
		}

		return var;
	}

	public int number() {
		int r = digit();
		while ((this.next().charAt(0) >= '0' && this.next().charAt(0) <= '9')) {
			r *= 10;
			r += digit();
		}
		return r;
	}

	public String relOp() {
		String x = this.advance();
		if (x.equals("==") || x.equals("!=") || x.equals("<") || x.equals(">")
				|| x.equals("<=") || x.equals(">="))
			return x;
		else {
			printError("relop");
			return null;
		}
	}

	public void relation(char branch) {
		String v1 = this.expression();
		String rel = this.relOp();
		String v2 = this.expression();
		addLine("cmp", v1, v2);
		String temp = "(" + String.valueOf((curLine - 1)) + ")";
		String bto = "" + branch;
		if (rel.equals("=="))
			addLine("bne", temp, bto);
		else if (rel.equals("!="))
			addLine("beq", temp, bto);
		else if (rel.equals("<="))
			addLine("bgt", temp, bto);
		else if (rel.equals(">="))
			addLine("blt", temp, bto);
		else if (rel.equals(">"))
			addLine("ble", temp, bto);
		else if (rel.equals("<"))
			addLine("bge", temp, bto);
		lines.get(curLine - 1).sink = null;

	}

	public static void addFourOPLine(String sink, String op, String opr1,
			String opr2) {
		SSANodes.get(curNode).addFourOPLine(sink, op, opr1, opr2);
	}

	public static boolean addLine(String op, String opr1, String opr2) {
		if (funSymbolTable.get(curFun).containsKey(opr1)) {
			varID ve = funSymbolTable.get(curFun).get(opr1);
			opr1 = ve.getVar(curNode);
			ve.addDuChain(curLine, 1);
		} else if (varIndex.containsKey(opr1)) {
			varIndex.get(opr1).addDuChain(curLine, 1);
		}
		if (funSymbolTable.get(curFun).containsKey(opr2)) {
			varID ve = funSymbolTable.get(curFun).get(opr2);
			opr2 = ve.getVar(curNode);
			ve.addDuChain(curLine, 2);
		} else if (varIndex.containsKey(opr2)) {
			varIndex.get(opr2).addDuChain(curLine, 2);
		}
		SSANodes.get(curNode).addLine(op, opr1, opr2);
		return false;
	}

	public String term() {
		String last = this.factor();
		last = this.getVarName(last);
		if (last == null) {
			printError("term");
			return null;
		}
		while (this.next().equals("*") || this.next().equals("/")) {
			String c = this.advance();
			String ft = this.getVarName(this.factor());
			if (ft == null) {
				printError("term");
				return null;
			} else {
				if (c.equals("*")) {
					if (last.charAt(0) == '#' && ft.charAt(0) == '#')
						last = "#"
								+ (Integer.valueOf(last.substring(1, last
										.length())) * Integer.valueOf(ft
										.substring(1, ft.length())));
					else {
						addLine("mul", last, ft);
						last = "(" + String.valueOf(curLine - 1) + ")";
					}

				} else {
					if (last.charAt(0) == '#' && ft.charAt(0) == '#')
						last = "#"
								+ (Integer.valueOf(last.substring(1, last
										.length())) / Integer.valueOf(ft
										.substring(1, ft.length())));
					else {
						addLine("div", last, ft);
						last = "(" + String.valueOf(curLine - 1) + ")";
					}
				}

			}
		}
		return last;
	}

	public boolean statSequence() {
		this.statement();
		while (this.next().equals(";")) {
			this.pos++;
			this.statement();
		}
		return true;
	}

	public boolean statement() {
		// System.out.println("**"+this.next());
		if (this.next().equals("let")) {
			this.assignment();
		} else if (this.next().equals("call")) {
			this.funcCall();
		} else if (this.next().equals("if")) {
			this.ifStatement();
		} else if (this.next().equals("while")) {
			this.whileStatement();
		} else if (this.next().equals("return")) {
			this.returnStatement();
		} else if (this.next().equals("OutputNum")) {
			this.outputNum();
		} else if (this.next().equals("OutputNewLine")) {
			// this.outputNewLine();
		} else if (this.next().equals("InputNum")) {
			// this.inputNum();
		} else {
			printError("statement");
			return false;
		}
		return true;
	}

	public void OutputNewLine() {
		if (this.next().equals("OutputNewLine")) {
			this.pos += 13;
			if (this.next().equals("(")) {
				this.pos++;
				if (this.next().equals(")")) {
					this.pos++;
					addLine("wnl", null, null);
				} else
					printError("outnl1");
			} else
				printError("outnl2");
		} else
			printError("outnl3");
	}

	public String InputNum() {
		if (this.next().equals("InputNum")) {
			this.pos += 8;
			if (this.next().equals("(")) {
				this.pos++;
				if (this.next().equals(")")) {
					this.pos++;
					addLine("read", null, null);
					return "(" + (curLine - 1) + ")";
				} else
					printError("read1");
			} else
				printError("read2");
		} else
			printError("read3");
		return null;
	}

	public void outputNum() {
		if (this.next().equals("OutputNum")) {
			this.pos += 9;
			if (this.next().equals("(")) {
				this.pos++;
				String s = this.expression();
				if (this.next().equals(")")) {
					this.pos++;
					if (s.charAt(0) == '#') {
						addLine("mov", s, null);
						String sink = lines.get(curLine - 1).sink;
						addLine("write", sink, null);
					} else
						addLine("write", s, null);
				} else {
					printError("on1");
				}
			} else {
				printError("on2");
			}
		} else {
			printError("on3");
		}
	}

	public Node PhiWhile(Node to) {
		for (varID ve : funSymbolTable.get(curFun).values()) {
			if (!ve.ini)
				continue;
			String PhiVar = this.updatePhiVar(ve.varName);
			to.addPhi(ve.varName, PhiVar, to.branVar.get(ve.varName), null);
		}
		return to;
	}

	public boolean delPhiWhileFun(String from, String to) {
		varID fve = compiler.varIndex.get(from);
		// System.out.println("$$"+to.substring(1, to.length()));
		fve.lastVersion = Integer.valueOf(to.substring(1, to.length() - 1));
		return true;
	}

	public boolean upDPWhile(Node to) {
		for (int i : to.label) {
			line l = lines.get(i);
			if (l.op.equals("Phi")) {
				String sub = l.getLn().substring(1, l.getLn().length() - 1);
				// System.out.println(l.opr1+" $$ "+sub);
				compiler.varIndex.get(l.getLn()).lastVersion = Integer
						.valueOf(sub);
			}

		}
		return true;
	}

	public void delPhiWhile(Node to) {

		for (int temp = 0; temp < to.label.size(); temp++) {
			Integer ix = to.label.get(temp);
			line l = lines.get(ix);
			// System.out.println(ix+" ::::: "+l.op+" "+l.opr1+" "+l.opr2);

			if (l.op.equals("Phi") && l.getLn().equals(l.opr1)) {
				varID delV = varIndex.get(l.getLn());
				delV.removeDuChainLine(ix);
				to.label.remove(temp--);
			}
			if (l.op.equals("Phi") && l.opr2 == null) {
				// System.out.println(ix+": "+l.opr1+" "+l.opr2);
				varID delV = varIndex.get(l.getLn());
				for (int j = to.whileStartLN; j < to.whileEndLN; j++) {
					line local = lines.get(j);
					if (l.sink.equals(local.sink))
						continue;
					if (local.opr2 != null) {
						if (local.opr2.equals(l.getLn())) {
							this.delPhiWhileFun(local.opr2, l.opr1);
							local.opr2 = l.opr1;
						}
					}
					if (local.opr1 != null) {
						if (local.opr1.equals(l.getLn())) {
							this.delPhiWhileFun(local.opr1, l.opr1);
							local.opr1 = l.opr1;
						}
					}
					// System.out.println(j+" ~~ "+local.op+" "+local.opr1+"
					// "+local.opr2);

				}
				delV.removeDuChainLine(ix);
				to.label.remove(temp--);
			}
			if (l.op.equals("Phi") && l.opr2 != null) {
				// System.out.println(ix+"::: "+l.opr1+" "+l.opr2);

				for (int j = to.whileStartLN; j < to.whileEndLN; j++) {
					line local = lines.get(j);
					if (l.sink.equals(local.sink))
						continue;
					// System.out.println(local.op+" ## "+local.opr1+"
					// "+local.opr2);
					if (local.opr2 != null) {
						if (local.opr2.equals(l.opr1)) {
							// System.out.println("@@ "+local.opr1+"
							// "+local.opr2);
							this.delPhiWhileFun(local.opr2, l.getLn());
							local.opr2 = l.getLn();
						}
					}
					if (local.opr1 != null) {
						if (local.opr1.equals(l.opr1)) {
							this.delPhiWhileFun(local.opr1, l.getLn());
							local.opr1 = l.getLn();
						}
					}
					// System.out.println(j+" ~~ "+local.op+" "+local.opr1+"
					// "+local.opr2);

				}
			}

		}
		this.upDPWhile(to);
	}

	public boolean whileStatement() {

		if (this.next().equals("while")) {
			this.pos += 5;

			Node x = new Node(SSANodes.size());
			x.state = 8;
			int old = curNode;
			SSANodes.get(curNode).branchNum = 1;
			x.domF = SSANodes.get(curNode).domF;

			SSANodes.add(x);
			SSANodes.get(curNode).next = SSANodes.indexOf(x);

			this.addSucc(SSANodes.get(curNode), x);
			this.addPre(x, SSANodes.get(curNode));

			SSANodes.get(curNode).children.add(x);
			x.parent = SSANodes.get(curNode);
			this.copyState(x);
			x.whileStartLN = curLine;
			x = this.PhiWhile(x);

			curNode = SSANodes.indexOf(x);
			this.relation('@');
			if (!this.next().equals("do")) {
				printError("while1");
				return false;
			}
			this.pos += 2;
			Node z = new Node(SSANodes.size());
			SSANodes.add(z);
			z.domF = SSANodes.indexOf(x);
			// z.branchNum = 2;
			this.copyState(z);
			Node y = new Node(SSANodes.size());
			SSANodes.add(y);
			y.domF = SSANodes.get(old).domF;
			SSANodes.get(curNode).next = SSANodes.indexOf(z);
			curNode = SSANodes.indexOf(z);

			this.statSequence();
			Node zLastNode = SSANodes.get(curNode);
			if (!this.next().equals("od")) {
				printError("while2");
				return false;
			}
			this.pos += 2;
			zLastNode.next = SSANodes.indexOf(x);
			zLastNode.branchNum = 2;
			x.whileEndLN = curLine;
			this.delPhiWhile(x);
			x.children.add(y);
			y.parent = x;
			x.children.add(z);
			z.parent = x;
			curNode = SSANodes.indexOf(y);
			this.updateIf(SSANodes.indexOf(x), SSANodes.indexOf(y));
		} else {
			printError("while");
			return false;
		}

		return true;
	}

	public boolean returnStatement() {
		this.pos += 6;
		if (this.isIdent(this.next())) {
			String r = this.expression();

			// store global
			for (String var : globalVars.keySet()) {
				compiler.lineIndex.put(compiler.curLine, SSANodes.get(curNode));
				SSANodes.get(curNode).label.add(compiler.curLine++);
				String temp;
				varID ve = funSymbolTable.get(curFun).get(var);
				temp = ve.getVar(curNode);
				line l = new line(temp, "gStore", "0", "~"
						+ globalVars.get(var));
				compiler.lines.add(l);
			}

			// load FP
			String FP = this.loadFP();

			// store return value
			if (r.charAt(0) == '#') {
				addFourOPLine("(" + curLine + ")", "add", "0", r);
				addFourOPLine("(" + (curLine - 1) + ")", "skStore", FP, "#-4");
			} else
				addFourOPLine(r, "skStore", FP, "#-4");

			// load return addr
			String retAddr = "(" + curLine + ")";
			addFourOPLine(retAddr, "skLoad", FP, "#-8");

			// back to caller
			addFourOPLine(null, "return", "&" + curNode, retAddr);

			// mark as not void
			funMap.get(curFun).isVoid = false;
		}
		return true;
	}

	public String factor() {
		String next = this.next();
		if (next.equals("call")) {
			return this.funcCall();
		} else if (next.equals("(")) {
			this.advance();
			String e = this.expression();
			if (this.advance().equals(")"))
				return e;
			else {
				printError("factor1");
				return null;
			}
		} else if (this.isNumber(next)) {
			return String.valueOf(this.number());
		} else if (this.isIdent(next)) {
			String var = this.designator();
			if (var.charAt(0) == '`') {
				addFourOPLine("(" + curLine + ")", "arrLoad", var.substring(1),
						"0");
				return "(" + (curLine - 1) + ")";
			}
			return var;
		} else {
			printError("factor2" + next);
			return null;
		}
	}

	public boolean isIdent(String s) {
		for (char i : s.toCharArray()) {
			if (!((i >= 'A' && i <= 'Z') || (i >= 'a' && i <= 'z') || (i >= '0' && i <= '9'))) {
				return false;
			}
		}
		return true;

	}

	public boolean isNumber(String s) {
		for (char i : s.toCharArray()) {
			if (!(i >= '0' && i <= '9')) {
				return false;
			}
		}
		return true;

	}

	public String loadFP() {
		String FPReg = "(" + curLine + ")";
		addFourOPLine(FPReg, "skLoad", "0", "!" + FPPos);
		return FPReg;
	}

	public void updateFP(String FPReg, int count) {
		addFourOPLine("(" + curLine + ")", "add", FPReg, "#" + 4 * count);
		addFourOPLine("(" + (curLine - 1) + ")", "skStore", "0", "!" + FPPos);
	}

	public void printLastLine() {
		// line l = lines.get(curLine-1);
		// System.out.println(lines.indexOf(l)+ " "+l.sink+" "+l.op+" "+l.opr1+"
		// "+l.opr2);
	}

	public String funcCall() {
		if (!this.next().equals("call")) {
			printError("funCall1");
			return null;
		}
		this.pos += 4;
		String fname = this.ident();
		if (fname.equals("InputNum")) {
			this.pos -= 8;
			return this.InputNum();
		}
		if (fname.equals("OutputNum")) {
			this.pos -= 9;
			this.outputNum();
			return null;
		}
		if (fname.equals("OutputNewLine")) {
			this.pos -= 13;
			this.OutputNewLine();
			return null;
		}
		ArrayList<String> para = new ArrayList<String>();
		if (this.next().equals("(")) {
			this.pos++;
			if (this.isIdent(this.next())) {
				String st = this.expression();
				if (st.charAt(0) == '#') {
					addLine("mov", st, null);
					para.add(lines.get(curLine - 1).sink);
				} else
					para.add(st);
				while (this.next().equals(",")) {
					this.pos++;
					String sst = this.expression();
					if (sst.charAt(0) == '#') {
						addLine("mov", sst, null);
						para.add(lines.get(curLine - 1).sink);
					} else
						para.add(sst);
				}
			}
			if (!this.next().equals(")")) {
				printError("funCall3");
				return null;
			}
			this.pos++;
		}
		funInfo fif = this.funMap.get(fname);

		int count = 1;
		// load FP
		String FPReg = this.loadFP();

		// add local vars
		if (curFun != "main") {
			Map<String, varID> funTB = funSymbolTable.get(curFun);
			for (String s : funTB.keySet()) {
				if (para.contains(s) || globalVars.containsKey(s))
					continue;
				// System.out.println("!!"+s);
				addFourOPLine(funTB.get(s).getVar(curNode), "skStore", FPReg,
						"#" + 4 * (count++));
				this.printLastLine();
			}
		}

		// add parameters
		for (int i = 0; i < para.size(); i++) {
			addFourOPLine(para.get(i), "skStore", FPReg, "#" + 4 * (count++));
			this.printLastLine();
		}

		// add global vars
		for (String var : globalVars.keySet()) {

			varID ve = funSymbolTable.get(curFun).get(var);
			// if(para.contains(ve.getVar(curNode)))
			// continue;
			String temp;
			// if(ve.lastVersion == ve.iniVersion)
			// temp = "0";
			// else
			temp = ve.getVar(curNode);
			compiler.lineIndex.put(compiler.curLine, SSANodes.get(curNode));
			SSANodes.get(curNode).label.add(compiler.curLine++);
			line l = new line(temp, "gStore", "0", "~" + globalVars.get(var));
			compiler.lines.add(l);
		}

		Node n = new Node(SSANodes.size());
		SSANodes.add(n);
		this.addDomRoots(n);

		// add return addr
		addFourOPLine("(" + curLine + ")", "retAddr", "0", "#" + n.title);
		addFourOPLine("(" + (curLine - 1) + ")", "skStore", FPReg, "#" + 4
				* (count++));
		this.printLastLine();

		// add result
		addFourOPLine("0", "skStore", FPReg, "#" + 4 * (count++));
		this.printLastLine();

		// add oldFP
		addFourOPLine(FPReg, "skStore", FPReg, "#" + 4 * (count++));
		this.printLastLine();

		// update FP
		this.updateFP(FPReg, count - 1);
		this.printLastLine();

		// bsr to fun
		this.addBsr(SSANodes.get(curNode), SSANodes.get(fif.start));
		SSANodes.get(fif.start).domF = SSANodes.get(curNode).domF;
		n.domF = SSANodes.get(curNode).domF;
		SSANodes.get(fif.start).parent = SSANodes.get(curNode);
		this.addSucc(SSANodes.get(curNode), SSANodes.get(fif.start));
		this.addSucc(SSANodes.get(fif.end), n);
		this.addPre(SSANodes.get(fif.start), SSANodes.get(curNode));
		this.addPre(n, SSANodes.get(fif.end));
		extendEdges += "edge: { sourcename: " + '"' + curNode + '"'
				+ "\ntargetname: " + '"' + fif.start + '"' + "\ncolor: red\n}";
		extendEdges += "edge: { sourcename: " + '"' + fif.end + '"'
				+ "\ntargetname: " + '"' + n.title + '"' + "\ncolor: red\n}";

		curNode = SSANodes.indexOf(n);

		// back to caller
		if (fif.isVoid)
			this.addBsr(SSANodes.get(fif.end), SSANodes.get(curNode));
		// load lastFP
		String lastFP = this.loadFP();

		// load result
		String result = "(" + curLine + ")";
		addFourOPLine(result, "skLoad", lastFP, "#-4");

		// load nowFP
		String nowFP = "(" + curLine + ")";
		addFourOPLine(nowFP, "skLoad", lastFP, "0");

		//				
		// load paras
		count = -(para.size() + 2);
		for (int i = 0; i < para.size(); i++) {
			addFourOPLine(para.get(i), "skLoad", nowFP, "#" + 4 * (count++));
		}

		// //load old FP
		//	
		count = 1;
		// load local vars
		if (curFun != "main") {

			Map<String, varID> funTB = funSymbolTable.get(curFun);
			for (String s : funTB.keySet()) {
				if (para.contains(s) || globalVars.containsKey(s))
					continue;
				addFourOPLine(funTB.get(s).getVar(curNode), "skLoad", nowFP,
						"#" + 4 * (count++));
			}
		}

		// update FP
		addFourOPLine(nowFP, "skStore", "0", "!" + FPPos);

		// load global vars
		for (String var : globalVars.keySet()) {
			varID ve = funSymbolTable.get(curFun).get(var);
			ve.lastVersion = compiler.curLine;
			compiler.lineIndex.put(compiler.curLine, SSANodes.get(curNode));
			SSANodes.get(curNode).label.add(compiler.curLine++);
			line ll = new line(ve.getVar(curNode), "gLoad", "0", "~"
					+ globalVars.get(var));
			compiler.lines.add(ll);
		}

		return result;
	}

	public boolean addSucc(Node from, Node to) {
		if (from.succ.contains(to))
			return false;
		from.succ.add(to);
		return true;
	}

	public boolean addPre(Node from, Node to) {
		if (from.pre.contains(to))
			return false;
		from.pre.add(to);
		return true;
	}

	public void loadLocalVars() {

	}

	public String designator() {
		String var = this.ident();
		ArrayList<String> aps = new ArrayList<String>();
		while (this.next().equals("[")) {
			this.pos++;
			String temp = this.expression();
			aps.add(temp);
			if (this.next().equals("]")) {
				this.pos++;
			} else {
				printError("designator1");
				return null;
			}
		}
		if (aps.size() > 0) {
			return "`" + this.desFun(aps, var);
		}

		return var;
	}

	public String desFun(ArrayList<String> x, String aName) {
		ArrayList<Integer> y = this.arrParas.get(aName);
		int base = this.arrTable.get(aName);
		if (x.size() != y.size()) {
			printError("desFun0");
			return null;
		}
		String result = "(" + curLine + ")";
		addFourOPLine(result, "adda", x.get(x.size() - 1), "#" + base);
		if (x.size() == 1) {
			addFourOPLine(result, "mula", result, "#4");
			return result;
		}
		String amul = "(" + curLine + ")";
		addFourOPLine(amul, "adda", "0", "#1");
		for (int i = x.size() - 2; i >= 0; i--) {
			addFourOPLine(amul, "mula", amul, "#" + y.get(i + 1));
			addFourOPLine("(" + curLine + ")", "mula", amul, x.get(i));
			addFourOPLine(result, "adda", result, "(" + (curLine - 1) + ")");
		}
		addFourOPLine(result, "mula", result, "#4");
		return result;// "("+(curLine-1)+")";

	}

	public int typeDecl() {
		if (this.next().equals("var")) {
			this.pos += 3;
			return -1;
		} else if (this.next().equals("array")) {
			this.pos += 5;
			int size = 0;
			ArrayList<Integer> temp = new ArrayList<Integer>();

			if (this.next().equals("[")) {
				this.pos++;
				size = this.number();
				temp.add(size);
				if (!this.next().equals("]")) {
					printError("typeDecl");
					return -1;
				}
				this.pos++;
				while (this.next().equals("[")) {
					this.pos++;
					int x = this.number();
					size *= x;
					temp.add(x);
					if (!this.next().equals("]")) {
						printError("typeDecl");
						return -1;
					}
					this.pos++;
				}
				String arrName = this.next();
				this.arrParas.put(arrName, temp);
				return size;
				// //////dec array
			}
		} else {
			printError("typeDec3");

		}
		return -1;

	}

	public boolean varDecl(boolean flag, int size) {
		if (flag) {
			size = this.typeDecl();
		}
		if (size > 0) {
			String var = this.ident();
			if (arrTable.containsKey(var)) {
				printError("varDecl6");
				return false;
			}
			arrTable.put(var, arrayStackTop);
			arrayStackTop += size;
			while (this.next().equals(",")) {
				this.pos++;
				String nv = this.ident();
				this.arrParas.put(nv, this.arrParas.get(var));
				arrTable.put(nv, arrayStackTop);
				arrayStackTop += size;
			}
			if (flag) {

				if (!this.next().equals(";")) {
					printError("varDec5 " + this.next());
					return false;
				}
				this.pos++;
			}
		} else {
			String var = this.ident();
			Map<String, varID> symbolTable = funSymbolTable.get(curFun);
			if (symbolTable.containsKey(var)) {
				printError("varDecl3 " + var);
				return false;
			}
			varID vid = new varID(var);
			symbolTable.put(var, vid);
			funSymbolTable.put(curFun, symbolTable);
			if (this.next().equals(",")) {
				this.pos++;
				this.varDecl(false, size);
			}
			if (flag) {
				if (!this.advance().equals(";")) {
					printError("varDecl");
					return false;
				}
			}
		}

		return true;
	}

	public boolean formalParam(String funName) {
		ArrayList<String> paraList = new ArrayList<String>();
		if (!this.advance().equals("(")) {
			printError("formalParam1");
			return false;
		}

		// read parameters
		if (!this.next().equals(")")) {
			Map<String, varID> newSymTab = funSymbolTable.get(curFun);
			String temp = this.ident();

			paraList.add(temp);

			while (this.next().equals(",")) {
				this.pos++;
				temp = this.ident();

				if (newSymTab.containsKey(temp)) {
					printError("formalParam3");
					return false;
				}
				paraList.add(temp);
			}
			if (!this.advance().equals(")")) {
				printError("formalParam4");
				return false;
			}

			// read FP
			String FP = this.loadFP();

			int count = -paraList.size() - 2;

			// load parameters
			for (String par : paraList) {
				varID ve = new varID(par);
				newSymTab.put(par, ve);
				addFourOPLine(ve.getVar(curNode), "skLoad", FP, "#"
						+ (4 * count++));
				this.printLastLine();
			}

			this.funMap.get(funName).paraList = paraList;
			// stackMap.put(funName, paraStackIx);
			funSymbolTable.put(curFun, newSymTab);
		} else {
			this.pos++;

		}

		return true;
	}

	public boolean funcBody() {
		while (this.next().equals("var") || this.next().equals("array")) {
			this.varDecl(true, -1);
		}

		if (!this.advance().equals("{")) {
			printError("funcBody1");
			return false;
		}
		this.statSequence();
		if (!this.advance().equals("}")) {
			printError("funcBody2");
			return false;
		}
		return true;
	}

	public void addDomRoots(Node n) {
		if (domRoots.contains(n))
			return;
		domRoots.add(n);
	}

	public boolean funDecl() {
		String funType = this.next();
		if (funType.equals("function") || funType.equals("procedure")) {
			if (funType.equals("function"))
				this.pos += 8;
			else
				this.pos += 9;
			// initial fun info
			String funName = this.ident();
			String oldFunName = curFun;
			curFun = funName;
			Node fn = new Node(SSANodes.size());
			SSANodes.add(fn);
			int old = curNode;
			curNode = SSANodes.indexOf(fn);
			funSymbolTable.put(curFun, new HashMap<String, varID>());
			this.funMap.put(funName, new funInfo(SSANodes.indexOf(fn)));

			this.addDomRoots(fn);

			if (this.next().equals("("))
				this.formalParam(funName);
			// load global vars
			for (String var : globalVars.keySet()) {
				varID ve = new varID(var);
				funSymbolTable.get(curFun).put(var, ve);
				compiler.lineIndex.put(compiler.curLine, SSANodes.get(curNode));
				SSANodes.get(curNode).label.add(compiler.curLine++);
				line l = new line(ve.getVar(curNode), "gLoad", "0", "~"
						+ globalVars.get(var));
				compiler.lines.add(l);
			}

			if (!this.advance().equals(";")) {
				printError("fDel1");
				return false;
			}
			this.funcBody();
			if (!this.advance().equals(";")) {
				printError("fDel2");
				return false;
			}
			this.funMap.get(funName).end = curNode;

			curNode = old;
			curFun = oldFunName;
		}
		return true;
	}

	public void makeGlobalVars() {
		for (String var : globalVars.keySet()) {
			varID ve = new varID(var);
			funSymbolTable.get(curFun).put(var, ve);
		}
	}

	public void updateFunSymbol(String from, String to) {
		Map<String, varID> fromTable = funSymbolTable.get(from);

		for (String s : fromTable.keySet()) {
			if (funSymbolTable.get(to).containsKey(s))
				funSymbolTable.get(to).put(s, fromTable.get(s));
		}
	}

	public boolean concatTable(String oldNode, String newNode,
			ArrayList<String> paraList) {
		Map<String, varID> oldv = funSymbolTable.get(oldNode);
		Map<String, varID> newv = funSymbolTable.get(newNode);
		for (String s : paraList) {
			oldv.put(s, newv.get(s));
		}
		funSymbolTable.put(oldNode, oldv);
		return false;
	}

	public boolean computation() {

		if (this.next().equals("main")) {
			this.pos += 4;

			stangeSet.add("retAddr");
			stangeSet.add("skStore");
			stangeSet.add("skLoad");
			stangeSet.add("gLoad");
			stangeSet.add("gStore");
			stangeSet.add("spStore");
			stangeSet.add("spLoad");
			stangeSet.add("arrStore");
			stangeSet.add("arrLoad");
			stangeSet.add("adda");
			stangeSet.add("mula");

			Map<String, varID> mainS = new HashMap<String, varID>();
			funSymbolTable.put("main", mainS);
			curFun = "main";
			Node n = new Node(SSANodes.size());
			SSANodes.add(n);
			domRoots.add(n);
			mainStart = SSANodes.indexOf(n);
			curNode = SSANodes.indexOf(n);

			while (this.next().equals("var") || this.next().equals("array")) {
				if (this.next().equals("var")) {
					this.varDecl(true, -1);
				} else if (this.next().equals("array")) {
					this.varDecl(true, -1);
				}
			}
			for (String var : funSymbolTable.get(curFun).keySet()) {
				globalVars.put(var, globalStackTop++);
			}
			while (this.next().equals("function")
					|| this.next().equals("procedure")) {

				if (this.next().equals("function")
						|| this.next().equals("procedure")) {
					this.funDecl();
				}

			}

			if (this.next().equals("{")) {

				this.pos++;
				this.statSequence();
				if (this.advance().equals("}") && this.next().equals(".")) {
					return true;
				} else {
					printError("computation0");
					return false;
				}
			} else {
				printError("computation1");
				return false;
			}
		} else {
			printError("computation2");
			return false;
		}
	}

	public Node copyState(Node to) {
		Map<String, varID> st = funSymbolTable.get(curFun);
		for (String var : st.keySet()) {
			to.branVar.put(var, st.get(var).getVar(curNode));
			to.branFlag.put(var, true);
		}
		return to;
	}

	// //////error ex: ifi ... if i
	public boolean ifStatement() {
		int oldNode = curNode;
		if (this.next().equals("if")) {
			this.pos += 2;
			this.relation('@');
			if (this.next().equals("then")) {
				this.pos += 4;
				Node x = new Node(SSANodes.size());

				x.state = 1;
				x.branchNum = 1;
				x = this.copyState(x);
				SSANodes.add(x);
				SSANodes.get(curNode).next = SSANodes.indexOf(x);

				Node y = new Node(SSANodes.size());

				y.state = 2;
				y.branchNum = 2;
				y = this.copyState(y);
				Node z = new Node(SSANodes.size());
				z.state = 3;// SSANodes.get(curNode).state;
				z.branchNum = SSANodes.get(curNode).branchNum;
				z.domF = SSANodes.get(curNode).domF;
				SSANodes.add(z);
				z = this.copyState(z);
				x.domF = SSANodes.indexOf(z);
				y.domF = SSANodes.indexOf(z);
				curNode = SSANodes.indexOf(x);
				this.statSequence();
				Node xBrachLastNode = SSANodes.get(curNode);
				Node yBrachLastNode = null;
				boolean elseFlag = false;
				if (this.next().equals("else")) {
					elseFlag = true;
					this.pos += 4;
					y.title = String.valueOf(SSANodes.size());
					SSANodes.add(y);

					curNode = SSANodes.indexOf(y);

					this.statSequence();
					yBrachLastNode = SSANodes.get(curNode);
				}

				if (this.next().equals("fi")) {
					this.pos += 2;
					if (x.label.size() == 0) {
					}
					curNode = SSANodes.indexOf(z);
					if (elseFlag) {
						SSANodes.get(oldNode).children.add(y);
						y.parent = SSANodes.get(oldNode);
						this.updateIf(oldNode, SSANodes.indexOf(y));
						yBrachLastNode.next = SSANodes.indexOf(z);
					} else {
						this.updateIf(oldNode, SSANodes.indexOf(z));
					}
					z = this.copyState(z);
					xBrachLastNode.next = SSANodes.indexOf(z);

					SSANodes.get(oldNode).children.add(x);
					x.parent = SSANodes.get(oldNode);
					SSANodes.get(oldNode).children.add(z);
					z.parent = SSANodes.get(oldNode);

					return true;
				} else {
					printError("if1");
					return false;
				}
			} else {
				printError("if3");
				return false;
			}

		} else {
			printError("if4");
			return false;
		}

	}

	public boolean addBsr(Node n, Node next) {
		n.label.add(compiler.curLine++);
		line l = new line(null, "bsr", null, next.title);
		compiler.lines.add(l);
		return true;
	}

	public boolean updateIf(int old, int y) {

		ArrayList<Integer> label = SSANodes.get(old).label;
		for (int i : label) {
			line l = lines.get(i);
			if (l.opr1 != null)
				if (l.opr1.equals("@")) {
					printError("updateIF");
				}
			if (l.opr2 != null)
				if (l.opr2.equals("@")) {
					l.opr2 = String.valueOf(SSANodes.get(y).title);

				}
		}
		extendEdges += "edge: { sourcename: " + '"' + old + '"'
				+ "\ntargetname: " + '"' + y + '"' + "\ncolor: red\n}";
		if (!SSANodes.get(old).succ.contains(SSANodes.get(y)))
			SSANodes.get(old).succ.add(SSANodes.get(y));
		if (!SSANodes.get(y).pre.contains(SSANodes.get(old)))
			SSANodes.get(y).pre.add(SSANodes.get(old));
		return true;
	}

	public String expression() {
		String last = this.term();
		while (this.next().equals("+") || this.next().equals("-")) {
			String c = this.advance();
			String ft = this.term();
			if (ft == null) {
				printError("expression");
				return null;
			} else {

				if (c.equals("+")) {
					if (last.charAt(0) == '#' && ft.charAt(0) == '#')
						last = "#"
								+ (Integer.valueOf(last.substring(1, last
										.length())) + Integer.valueOf(ft
										.substring(1, ft.length())));
					else {
						addLine("add", last, ft);
						last = "(" + String.valueOf(curLine - 1) + ")";
					}
				} else {
					if (last.charAt(0) == '#' && ft.charAt(0) == '#')
						last = "#"
								+ (Integer.valueOf(last.substring(1, last
										.length())) - Integer.valueOf(ft
										.substring(1, ft.length())));
					else {
						addLine("sub", last, ft);
						last = "(" + String.valueOf(curLine - 1) + ")";
					}
				}
			}
		}
		return last;
	}

	// /////////wrong var version
	public String getVarName(String index) {
		if (funSymbolTable.get(curFun).containsKey(index)) {
			varID v = funSymbolTable.get(curFun).get(index);
			return v.getVar(curNode);
		} else if (index.charAt(0) == '(' || index.charAt(0) == '#'
				|| index.charAt(0) == '$') {
			return index;
		}
		return "#" + index;
	}

	public String updatePhiVar(String var) {
		if (!funSymbolTable.get(curFun).containsKey(var)) {
			printError("updateVar : no decl");
			System.exit(0);
			return null;
		}
		varID ve = funSymbolTable.get(curFun).get(var);
		ve.addDuChain(curLine, 0);
		String re = ve.updatePhiVar(curNode);
		if (!ve.varNodes.contains(curNode))
			ve.varNodes.add(curNode);
		funSymbolTable.get(curFun).put(var, ve);
		return re;
	}

	public String updateVar(String var, boolean flag) {
		if (!funSymbolTable.get(curFun).containsKey(var)) {
			printError("updateVar : no decl");
			System.exit(0);
			return null;
		}
		varID ve = funSymbolTable.get(curFun).get(var);
		ve.addDuChain(curLine - 1, 0);
		String newVV = null;
		if (flag) {
			ve.updateVar(curNode, flag);
			newVV = ve.getVar(curNode);
		} else {
			newVV = ve.updateVar(curNode, flag);
		}
		ve.ini = true;
		if (!ve.varNodes.contains(curNode))
			ve.varNodes.add(curNode);
		funSymbolTable.get(curFun).put(var, ve);
		return newVV;
	}

	public boolean copyPro(String var, String val) {
		if (!funSymbolTable.get(curFun).containsKey(var)) {
			printError("cp : no decl");
			System.exit(0);
			return false;
		}
		varID ve = funSymbolTable.get(curFun).get(var);
		if (funSymbolTable.get(curFun).containsKey(val)) {
			varID veval = funSymbolTable.get(curFun).get(val);
			ve.lastVersion = veval.lastVersion;
			return true;
		} else if (val.charAt(0) == '(') {
			ve.lastVersion = Integer
					.valueOf(val.substring(1, val.length() - 1));
			return true;
		} else {
			addLine("mov", val, "");
			return false;
		}
	}

	public boolean assignment() {
		if (this.next().equals("let")) {
			this.pos += 3;
			String var = this.designator();
			if (this.next().equals("<-")) {
				this.pos += 2;
				String val = this.expression();
				if (var.equals("Last")) {
				}
				if (var.charAt(0) == '`') {
					if (val.charAt(0) == '#') {
						addLine("mov", val, null);
						addFourOPLine("(" + (curLine - 1) + ")", "arrStore",
								var.substring(1), "0");
					} else
						addFourOPLine(val, "arrStore", var.substring(1), "0");
				} else {
					boolean rc = this.copyPro(var, val);
					String newPhiName = null;
					newPhiName = this.updateVar(var, rc);
					int df = SSANodes.get(curNode).domF;
					if (df != -1 && SSANodes.get(df).state == 8) {
					}
					if (df != -1) {
						int branch = SSANodes.get(curNode).branchNum;
						String sourceVar = SSANodes.get(curNode).branVar
								.get(var);
						this.insertPhi(df, branch, var, newPhiName, sourceVar);
					}
				}

				return true;
			} else {
				printError("ass1");
				return false;
			}
		} else {
			printError("ass2");
			return false;
		}
	}

	public boolean checkLocal(int df, String vname) {
		Node dfn = SSANodes.get(df);
		for (int i = dfn.whileStartLN; i < curLine; i++) {
			line l = lines.get(i);
			if (stangeSet.contains(l.op) || l.op.equals("Phi"))
				continue;
			if (varIndex.containsKey(l.opr1)) {
				if (varIndex.get(l.opr1).varName.equals(vname))
					return true;
			}
			if (varIndex.containsKey(l.opr2)) {
				if (varIndex.get(l.opr2).varName.equals(vname))
					return true;
			}

		}
		return false;
	}

	public void insertPhi(int b, int branchNum, String var, String newPhiName,
			String sourceVar) {
		int oldNode = curNode;
		while (b != -1) {
			curNode = b;
			Node n = SSANodes.get(b);
			varID ve = funSymbolTable.get(curFun).get(var);
			boolean flag = false;
			line l = null;
			for (int i : n.label) {
				l = lines.get(i);
				if (ve.DuChainContainLine(i) && l.op.equals("Phi")) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				String ov = ve.getVar(b);
				String PhiVar = this.updatePhiVar(var);
				if (n.state != 8) {
					if (branchNum == 1)
						n.addPhi(ve.varName, PhiVar, ov, sourceVar);
					else
						n.addPhi(ve.varName, PhiVar, sourceVar, ov);
				}

				b = n.domF;
				branchNum = n.branchNum;
				newPhiName = PhiVar;
				sourceVar = n.branVar.get(var);
				continue;
			} else {
				if (l == null) {
					printError("insertPhi");
					return;
				}
				if (branchNum == 1)
					l.opr1 = newPhiName; // ve.getVar(b);//ve.varName+ve.lastVersion;//ve.getVar(b);
				else {
					l.opr2 = newPhiName; // ve.getVar(b);//ve.varName+ve.lastVersion;//ve.getVar(b);
					if (!compiler.PhiVars.containsKey(l.opr2)
							&& compiler.lineIndex.get(l.getLNum()).state == 8)
						compiler.PhiVars.put(l.opr2, true);
				}

				newPhiName = l.getLn();
				branchNum = n.branchNum;
				b = n.domF;
				sourceVar = n.branVar.get(var);
				continue;
			}

		}
		curNode = oldNode;

	}

	public void RA() {
		ifg = new InfGraph();
		makeDUEChain();
		this.testDUChain();
		removePhi2(regN - 2);

		compiler.test("D:/compiler/ktemp.vcg");
		this.liveAna();
		ifg.color(regN - 2);

		test("D:/compiler/arp.vcg");
		updateReg();
		addBSR();
		addReturn();
	}

	public void updateReg() {
		for (Node n : SSANodes) {
			for (int i : n.label) {
				line l = lines.get(i);
				if (l.sink != null) {
					if (infMap.containsKey(l.sink)) {
						l.sink = "" + (infMap.get(l.sink).color + 1);
					}
				}
				if (l.opr1 != null) {
					if (infMap.containsKey(l.opr1)) {
						l.opr1 = "" + (infMap.get(l.opr1).color + 1);
					}
				}
				if (l.opr2 != null) {
					if (infMap.containsKey(l.opr2)) {
						l.opr2 = "" + (infMap.get(l.opr2).color + 1);
					}
				}
			}
		}
	}

	public boolean interfreColor(vertex x, ArrayList<vertex> y) {
		for (vertex vy : y) {
			if (x.color == vy.color)
				return true;
		}
		return false;
	}

	public boolean removePhiCmpColor(ArrayList<vertex> x, ArrayList<vertex> y) {
		for (vertex vx : x)
			for (vertex vy : y) {
				if (vx.color == vy.color)
					return true;
			}
		return false;
	}

	public boolean colorAble(vertex sink, vertex opr) {
		for (vertex v : opr.neighbor) {
			if (v.color == sink.color)
				return false;
		}
		return true;
	}

	public void removePhi2(int n) {
		for (int temp = PhiLines.size() - 1; temp >= 0; temp--) {
			int i = PhiLines.get(temp);
			line l = lines.get(i);

			for (Node p : lineIndex.get(i).pre) {
				if (p.branchNum == 1)
					p.removePhiAddLine(l.opr1, l.sink);
			}

			for (Node p : lineIndex.get(i).pre) {
				if (p.branchNum == 2)
					p.removePhiAddLine(l.opr2, l.sink);
			}

			lineIndex.get(i).label.remove(lineIndex.get(i).label.indexOf(i));
			if (useChain.containsKey(l.opr1))
				useChain.get(l.opr1).remove(useChain.get(l.opr1).indexOf(i));
			if (useChain.containsKey(l.opr2))
				useChain.get(l.opr2).remove(useChain.get(l.opr2).indexOf(i));
			if (defChain.contains(l.sink)) {
				defChain.remove(defChain.indexOf(l.sink));
				defChainLn.remove(defChainLn.indexOf(i));
			}
		}
	}

	public boolean interfre(ArrayList<vertex> a, vertex b) {

		for (vertex x : a) {
			if (x.neighbor.contains(b) || b.neighbor.contains(x))
				return true;
		}
		return false;
	}

	public void printAL(ArrayList<String> a, String exd) {
	}

	public boolean checkALEq(ArrayList<String> x, ArrayList<String> y) {
		if (x == null) {
			if (y == null)
				return true;
			else
				return false;
		}
		if (y == null) {
			if (x == null)
				return true;
			else
				return false;
		}
		if (x.size() != y.size())
			return false;
		for (int i = 0; i < x.size(); i++) {
			if (!x.get(i).equals(y.get(i)))
				return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public void liveAna() {
		@SuppressWarnings("unused")
		int times = 0;
		while (true) {

			boolean flag = false;
			for (int i = SSANodes.size() - 1; i >= 0; i--) {

				Node n = SSANodes.get(i);
				// System.out.println("//"+n.title);
				this.printAL(n.in, "before in");
				this.printAL(n.out, "before out");
				ArrayList<String> tempin = (ArrayList<String>) n.in.clone();
				ArrayList<String> tempout = (ArrayList<String>) n.out.clone();
				for (Node ni : n.succ) {
					for (String s : ni.in) {
						if (!n.out.contains(s))
							n.out.add(s);
					}
				}
				bottomUp(n);
				if (!this.checkALEq(n.in, tempin)
						|| !this.checkALEq(n.out, tempout))
					flag = true;
				this.printAL(n.in, "after in");
				this.printAL(n.out, "after out");
			}
			if (!flag)
				return;
		}

	}

	public void addEdge(String a, String b) {
		if (a.equals(b))
			return;
		vertex va = compiler.infMap.get(a);
		vertex vb = compiler.infMap.get(b);
		if (vb == null) {
		}

		if (!va.neighbor.contains(vb))
			va.neighbor.add(vb);
		if (!vb.neighbor.contains(va))
			vb.neighbor.add(va);
	}

	public static void insertStrangeSpillCode(int i, String vname, int stkpos) {
		line old = lines.get(i);
		if (old.opr1.equals(vname) && old.sink.equals(vname)) {
			Node n = compiler.lineIndex.get(i);
			int idx = n.label.indexOf(i);
			ArrayList<Integer> temp = compiler
					.subArrayList(n.label, 0, idx - 1);

			compiler.lineIndex.put(compiler.curLine, n);
			line l = new line("" + (regN - 1), "spLoad", "^" + vname, "#"
					+ stkpos);
			compiler.lines.add(l);

			temp.add(compiler.curLine++);
			temp
					.addAll(compiler.subArrayList(n.label, idx,
							n.label.size() - 1));
			n.label = temp;
			old.opr1 = "" + (regN - 1);
			old.sink = "" + (regN - 1);
		} else {
			if (old.opr1.equals(vname)) {
				Node n = compiler.lineIndex.get(i);
				int idx = n.label.indexOf(i);
				ArrayList<Integer> temp = compiler.subArrayList(n.label, 0,
						idx - 1);

				compiler.lineIndex.put(compiler.curLine, n);
				line l = new line("" + (regN - 1), "spLoad", "^" + vname, "#"
						+ stkpos);
				compiler.lines.add(l);

				temp.add(compiler.curLine++);
				temp.addAll(compiler.subArrayList(n.label, idx,
						n.label.size() - 1));
				n.label = temp;
				old.opr1 = "" + (regN - 1);
			}
			if (old.sink.equals(vname)) {
				Node n = compiler.lineIndex.get(i);
				int idx = n.label.indexOf(i);
				ArrayList<Integer> temp = compiler.subArrayList(n.label, 0,
						idx - 1);

				compiler.lineIndex.put(compiler.curLine, n);
				line l = new line("" + regN, "spLoad", "^" + vname, "#"
						+ stkpos);
				compiler.lines.add(l);

				temp.add(compiler.curLine++);
				temp.addAll(compiler.subArrayList(n.label, idx,
						n.label.size() - 1));
				n.label = temp;
				old.sink = "" + regN;
			}
		}
	}

	public static void insertSpillCode(String vname) {
		// insert load
		infMap.remove(vname);
		int stkPos = spillStackTop;
		spillVars.put(vname, spillStackTop);

		for (int i : compiler.useChain.get(vname)) {
			line old = lines.get(i);
			if (old.op.equals("skStore") || old.op.equals("gStore")
					|| old.op.equals("arrStore")) {
				insertStrangeSpillCode(i, vname, stkPos);
				continue;
			}
			if (old.opr1 != null && old.opr1.equals(vname)) {
				if (old.op.equals("Phi"))
					continue;
				Node n = compiler.lineIndex.get(i);
				int idx = n.label.indexOf(i);
				ArrayList<Integer> temp = compiler.subArrayList(n.label, 0,
						idx - 1);

				compiler.lineIndex.put(compiler.curLine, n);
				line l = new line("" + (regN - 1), "spLoad", "^" + vname, "#"
						+ stkPos);
				compiler.lines.add(l);

				temp.add(compiler.curLine++);
				temp.addAll(compiler.subArrayList(n.label, idx,
						n.label.size() - 1));
				n.label = temp;
				old.opr1 = "" + (regN - 1);
			} else if (old.opr2 != null && old.opr2.equals(vname)) {
				if (old.op.equals("Phi"))
					continue;
				Node n = compiler.lineIndex.get(i);
				int idx = n.label.indexOf(i);
				ArrayList<Integer> temp = compiler.subArrayList(n.label, 0,
						idx - 1);

				compiler.lineIndex.put(compiler.curLine, n);
				line l = new line("" + regN, "spLoad", "^" + vname, "#"
						+ stkPos);
				compiler.lines.add(l);

				temp.add(compiler.curLine++);
				temp.addAll(compiler.subArrayList(n.label, idx,
						n.label.size() - 1));
				n.label = temp;
				old.opr2 = "" + regN;
			} else if (old.sink != null && old.sink.equals(vname)) {
				// insert store
				if (old.op.equals("Phi"))
					continue;
				Node n = compiler.lineIndex.get(i);
				int idx = n.label.indexOf(i);
				ArrayList<Integer> temp = compiler
						.subArrayList(n.label, 0, idx);
				compiler.lineIndex.put(compiler.curLine, n);

				line l = new line("" + regN, "spStore", "^" + vname, "#"
						+ stkPos);
				compiler.lines.add(l);
				temp.add(compiler.curLine++);
				temp.addAll(compiler.subArrayList(n.label, idx + 1, n.label
						.size() - 1));
				n.label = temp;
				old.sink = "" + regN;
			} else {
				compiler.printError("insertSpill");
			}

		}

		// insert store
		int i = Integer.valueOf(vname.substring(1, vname.length() - 1));
		line old = lines.get(i);

		if (old.sink == vname && !old.op.equals("Phi")) {
			Node n = compiler.lineIndex.get(i);
			int idx = n.label.indexOf(i);
			ArrayList<Integer> temp = compiler.subArrayList(n.label, 0, idx);
			compiler.lineIndex.put(compiler.curLine, n);

			line l = new line("" + regN, "spStore", "^" + vname, "#" + stkPos);
			compiler.lines.add(l);
			temp.add(compiler.curLine++);
			temp.addAll(compiler.subArrayList(n.label, idx + 1,
					n.label.size() - 1));
			n.label = temp;
			old.sink = "" + regN;
		}

		spillStackTop++;
	}

	public static void addStoreCode(int i, String vname, int stkPos,
			String color, String op) {
		line old = lines.get(i);
		Node n = compiler.lineIndex.get(i);
		int idx = n.label.indexOf(i);
		ArrayList<Integer> temp = compiler.subArrayList(n.label, 0, idx);
		compiler.lineIndex.put(compiler.curLine, n);

		line l = new line("" + regN, "spStore", "^" + vname, "#" + stkPos);
		compiler.lines.add(l);
		temp.add(compiler.curLine++);
		temp
				.addAll(compiler.subArrayList(n.label, idx + 1,
						n.label.size() - 1));
		n.label = temp;
		old.sink = "" + regN;
	}

	public static void addLoadCode(int i, String vname, int stkPos,
			String color, String op) {
		line old = lines.get(i);
		Node n = compiler.lineIndex.get(i);
		int idx = n.label.indexOf(i);
		ArrayList<Integer> temp = compiler.subArrayList(n.label, 0, idx);
		compiler.lineIndex.put(compiler.curLine, n);

		line l = new line("" + regN, "spLoad", "^" + vname, "#" + stkPos);
		compiler.lines.add(l);
		temp.add(compiler.curLine++);
		temp
				.addAll(compiler.subArrayList(n.label, idx + 1,
						n.label.size() - 1));
		
		n.label = temp;
		old.sink = "" + regN;
	}

	public static ArrayList<Integer> subArrayList(ArrayList<Integer> A,
			int begin, int end) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (; begin <= end; begin++) {
			result.add(A.get(begin));
		}
		return result;
	}

	public void delVertex(String a) {
		vertex va = compiler.infMap.get(a);
		for (vertex v : va.neighbor)
			v.neighbor.remove(va);
		compiler.ifg.V.remove(va);
	}

	@SuppressWarnings("unchecked")
	public void bottomUp(Node n) {
		ArrayList<String> tempin = (ArrayList<String>) n.out.clone();
		for (int i = n.label.size() - 1; i >= 0; i--) {
			int ln = n.label.get(i);
			line l = lines.get(ln);
			if (stangeSet.contains(l.op)) {
				this.bottomUpFun(tempin, n.in, l.sink, l.op);
				this.bottomUpFun(tempin, n.in, l.opr1, l.op);
				this.bottomUpFun(tempin, n.in, l.opr2, l.op);
				continue;
			}
			if (l.op.equals("Phi")) {
				if (l.opr1.charAt(0) == '(') {
					for (Node p : n.pre) {
						if (p.branchNum == 1 && !p.out.contains(l.opr1)) {
							p.out.add(l.opr1);
						}

					}
				}// else{printError("bottomUp1");}
				if (l.opr2.charAt(0) == '(') {
					for (Node p : n.pre) {
						if (p.branchNum == 2 && !p.out.contains(l.opr2))
							p.out.add(l.opr2);
					}
				}// else{printError("bottomUp0");}

			}
			if (l.sink != null && l.sink.charAt(0) == '(') {
				tempin.remove(l.sink);
				n.in.remove(l.sink);
				for (String s : tempin) {
					addEdge(s, l.sink);
				}
			}// else{printError("bottomup2");}
			if (l.opr1 != null && l.opr1.length() > 1) {
				if (l.opr1.charAt(0) == '(') {
					for (String s : tempin) {
						addEdge(s, l.opr1);
					}
					if (!l.op.equals("Phi"))
						if (!tempin.contains(l.opr1))
							tempin.add(l.opr1);
					if (!l.op.equals("Phi"))
						if (!n.in.contains(l.opr1))
							n.in.add(l.opr1);
				}// else{printError("bottomup3");}
			}
			if (l.opr2 != null && l.opr2.length() > 1) {
				if (l.opr2.charAt(0) == '(') {
					for (String s : tempin) {
						addEdge(s, l.opr2);
					}
					if (!l.op.equals("Phi"))
						if (!tempin.contains(l.opr2))
							tempin.add(l.opr2);
					if (!l.op.equals("Phi"))
						if (!n.in.contains(l.opr2))
							n.in.add(l.opr2);
				}// else{printError("bottomup4");}
			}
		}
		n.in = tempin;
	}

	public void bottomUpFun(ArrayList<String> tempin, ArrayList<String> nin,
			String opr, String op) {
		if (opr != null && opr.length() > 1) {
			if (opr.charAt(0) == '(') {
				for (String s : tempin) {
					addEdge(s, opr);
				}
				if (!tempin.contains(opr))
					tempin.add(opr);
				if (!op.equals("Phi"))
					if (!nin.contains(opr))
						nin.add(opr);
			}// else{printError("bottomup4");}
		}
	}

	public static void addDefChain(String i, int n) {
		if (defChain.contains(i))
			return;
		defChain.add(i);
		defChainLn.add(n);
	}

	public static void addUseChain(String opr, int i) {
		if (opr == null)
			return;
		if (opr.length() < 1)
			return;
		// //System.out.println(opr);
		if (opr.charAt(0) == '#')
			return;
		if (opr.charAt(0) == '*')
			return;
		ArrayList<Integer> x = compiler.useChain.get(opr);
		if (x == null) {
			x = new ArrayList<Integer>();
		}
		x.add(i);
		compiler.useChain.put(opr, x);
	}

	// /////consider exceed
	private static String extendEdges = "";
	private String in = new String();
	private int pos = 0;
	static int curLine = 0;
	private static int curNode = 0;
	static ArrayList<line> lines = new ArrayList<line>();
	static ArrayList<Node> SSANodes = new ArrayList<Node>();
	static Map<String, Map<String, varID>> funSymbolTable = new HashMap<String, Map<String, varID>>();
	Map<String, funInfo> funMap = new HashMap<String, funInfo>();
	static Map<Integer, Node> lineIndex = new HashMap<Integer, Node>();
	static String curFun;
	static Map<String, varID> varIndex = new HashMap<String, varID>();
	static int regN = 8;
	static Map<String, vertex> infMap = new HashMap<String, vertex>();
	static InfGraph ifg;
	static Map<String, ArrayList<Integer>> useChain = new HashMap<String, ArrayList<Integer>>();
	static ArrayList<String> defChain = new ArrayList<String>();
	static ArrayList<Integer> defChainLn = new ArrayList<Integer>();
	static ArrayList<Integer> PhiLines = new ArrayList<Integer>();
	static Map<Integer, Integer> GCLineIndex = new HashMap<Integer, Integer>();
	static ArrayList<Integer> branchStack = new ArrayList<Integer>();
	// /////used to store paras' mem locations
	static int arrayStackTop = 0;
	static int spillStackTop = 0;
	static int globalStackTop = 0;
	static ArrayList<Integer> slStack = new ArrayList<Integer>();
	static ArrayList<Integer> spillStack = new ArrayList<Integer>();
	static ArrayList<Integer> globalVarStack = new ArrayList<Integer>();
	static ArrayList<Integer> FPStack = new ArrayList<Integer>();
	static ArrayList<Integer> retStack = new ArrayList<Integer>();
	static ArrayList<Integer> arrStack = new ArrayList<Integer>();
	static int FPPos = 0;
	static int mainStart;
	static Map<String, Integer> spillVars = new HashMap<String, Integer>();
	static Map<String, Integer> globalVars = new HashMap<String, Integer>();
	Map<String, Integer> arrTable = new HashMap<String, Integer>();
	Map<String, ArrayList<Integer>> arrParas = new HashMap<String, ArrayList<Integer>>();
	static Map<String, Boolean> PhiVars = new HashMap<String, Boolean>();
	static ArrayList<String> stangeSet = new ArrayList<String>();
	static ArrayList<Node> domRoots = new ArrayList<Node>();
}

class vertex {
	public vertex(String vName, int vLine) {
		this.vLine = vLine;
		name = vName;
	}

	public String printNeighbor() {
		String s = "";
		for (vertex nv : this.neighbor)
			s += nv.name;
		return s;
	}

	String name;
	int vLine;
	ArrayList<vertex> neighbor = new ArrayList<vertex>();
	int color = -1;
}

class InfGraph {
	public InfGraph() {
		for (int i = 0; i < compiler.defChain.size(); i++) {
			String vn = compiler.defChain.get(i);
			int ln = compiler.defChainLn.get(i);
			vertex v = new vertex(vn, ln);
			V.add(v);
			compiler.infMap.put(vn, v);
		}
	}

	public vertex getLessNb(int n) {
		for (vertex v : this.V) {
			if (v.neighbor.size() < n)
				return v;
		}
		return null;
	}

	public vertex pickSpill() {
		int maxV = 0;
		for (vertex v : this.V) {
			if (v.neighbor.size() > V.get(maxV).neighbor.size()
					&& !this.spillList.contains(v.name)) {
				maxV = V.indexOf(v);
			}

		}
		return V.get(maxV);
	}

	public boolean color(int n) {
		vertex dv = this.getLessNb(n);

		boolean flag = false;
		if (dv == null) {
			if (V.size() > 0) {
				dv = this.pickSpill();
				compiler.insertSpillCode(dv.name);
				flag = true;
			} else
				return true;
		}
		if (dv.neighbor.size() == 0) {
			dv.color = n - 1;
		}
		this.delVertex(dv);
		if (!this.color(n))
			return false;
		if (flag)
			return true;
		assignColor(dv, n);
		V.add(dv);
		return true;
	}

	public boolean assignColor(vertex v, int n) {
		boolean[] tempColor = new boolean[n];
		for (int i = 0; i < n; i++)
			tempColor[i] = false;
		for (vertex nv : v.neighbor) {
			if (V.contains(nv)) {
				tempColor[nv.color] = true;
				nv.neighbor.add(v);
			}

		}
		for (int i = 0; i < n; i++) {
			if (!tempColor[i]) {
				v.color = i;
				return true;
			}

		}
		return false;
	}

	public void addEdge(vertex va, vertex vb) {
		if (!va.neighbor.contains(vb))
			va.neighbor.add(vb);
		if (!vb.neighbor.contains(va))
			vb.neighbor.add(va);
	}

	public void delVertex(vertex va) {
		for (int i = 0; i < va.neighbor.size(); i++) {
			vertex v = va.neighbor.get(i);
			v.neighbor.remove(va);
		}

		this.V.remove(va);
	}

	ArrayList<vertex> V = new ArrayList<vertex>();
	Stack<vertex> tempStack = new Stack<vertex>();
	ArrayList<String> spillList = new ArrayList<String>();
}

class funInfo {
	public funInfo(int start) {
		this.start = start;
	}

	public int start;
	public int end;
	public boolean isVoid = true;
	ArrayList<String> paraList = new ArrayList<String>();
	public String returnAddr;
}

class Node {
	public Node(int title) {
		this.next = title;
		this.title = String.valueOf(title);
		this.nodeFun = compiler.curFun;
	}

	public String printLabel() {
		String r = "";
		for (int i : this.label) {
			line l = compiler.lines.get(i);
			if (l.opr3 != null) {
				r += l.opr3 + " [ " + l.op + " " + l.opr1 + " " + l.opr2
						+ " ]\n";
			} else {
				r += l.sink + " [ " + l.op + " " + l.opr1 + " " + l.opr2
						+ " ]\n";
			}

		}
		return r;
	}

	public int findLastAccur(String op) {
		for (int i = this.label.size() - 1; i >= 0; i--) {
			int ln = this.label.get(i);
			line l = compiler.lines.get(ln);
			if (l.opr1 != null && l.opr1.length() >= 1 && l.opr1.equals(op))
				return i;
			if (l.opr2 != null && l.opr2.length() >= 1 && l.opr2.equals(op))
				return i;
			if (l.sink != null && l.sink.length() >= 1 && l.sink.equals(op))
				return i;
		}
		return -1;
	}

	public void insertLine(int pos, line l) {

		ArrayList<Integer> temp = compiler.subArrayList(this.label, 0, pos);

		compiler.lineIndex.put(compiler.curLine, this);
		compiler.lines.add(l);

		temp.add(compiler.curLine++);
		temp.addAll(compiler.subArrayList(this.label, pos + 1, this.label
				.size() - 1));
		this.label = temp;
	}

	public void removePhiAddLine(String from, String to) {
		int pos1 = this.findLastAccur(from);
		if (pos1 == -1) {
			compiler.printError("remove Phi Line1");
		}
		int pos2 = this.findLastAccur(to);
		if (pos2 == -1) {
			compiler.printError("remove Phi Line2");
		}
		if (pos1 < pos2)
			pos1 = pos2;
		compiler.addUseChain(from, compiler.curLine);
		compiler.addUseChain(to, compiler.curLine);
		this.insertLine(pos1, new line(to, "mov", from, null));
	}

	public boolean addLine(String op, String opr1, String opr2) {
		compiler.lineIndex.put(compiler.curLine, this);
		this.label.add(compiler.curLine++);
		line l = new line("(" + (compiler.curLine - 1) + ")", op, opr1, opr2);
		compiler.lines.add(l);
		return true;
	}

	public boolean addFourOPLine(String sink, String op, String opr1,
			String opr2) {
		compiler.lineIndex.put(compiler.curLine, this);
		this.label.add(compiler.curLine++);
		line l = new line(sink, op, opr1, opr2);
		compiler.lines.add(l);
		return true;
	}

	public boolean addPhi(String varName, String newVar, String opr1,
			String opr2) {
		if (this.state == 8) {
			this.PhiFlag.put(varName, true);
			this.PhiVar.put(varName, newVar);
			compiler.lineIndex.put(compiler.curLine, this);
			compiler.PhiLines.add(compiler.curLine);
			compiler.lines.add(new line("(" + compiler.curLine + ")", "Phi",
					opr1, opr2));

			ArrayList<Integer> newLabel = new ArrayList<Integer>();
			newLabel.add(compiler.curLine++);
			newLabel.addAll(this.label);
			this.label = newLabel;
			return true;
		}
		this.PhiFlag.put(varName, true);
		this.PhiVar.put(varName, newVar);
		compiler.lineIndex.put(compiler.curLine, this);
		compiler.PhiLines.add(compiler.curLine);

		this.label.add(compiler.curLine++);
		line l = new line("(" + (compiler.curLine - 1) + ")", "Phi", opr1, opr2);
		compiler.lines.add(l);
		return true;
	}

	public void repUT(int ln) {
		line l = compiler.lines.get(ln);
		if (compiler.stangeSet.contains(l.op)) {
			if (l.sink != null && l.sink.length() > 0
					&& l.sink.charAt(0) == '(') {
				if (this.UT.containsKey((l.sink)))
					l.sink = this.UT.get(l.sink);
			}
		}
		if (this.UT.containsKey((l.opr1))) {
			l.opr1 = this.UT.get(l.opr1);
		}
		if (this.UT.containsKey((l.opr2))) {
			l.opr2 = this.UT.get(l.opr2);
		}
	}

	public String isInQT(int ln) {
		line l = compiler.lines.get(ln);
		if (isInPhi("(" + ln + ")", ln)) {
			return null;
		}
		if (!(l.op.equals("add") || l.op.equals("sub") || l.op.equals("mul")
				|| l.op.equals("div") || l.op.equals("cmp")
				|| l.op.equals("Phi") || l.op.equals("mov")))
			return null;
		if (this.QT.containsKey(l.toString()))
			return l.toString();
		if (l.op.equals("div"))
			return null;
		if (this.QT.containsKey(l.toResString()))
			return l.toResString();
		return null;
	}

	public void addQT(int ln) {
		line l = compiler.lines.get(ln);
		this.QT.put(l.toString(), new Pair<Integer>(ln, -1));
	}

	public boolean isInPhi(String opr, int ln) {
		if (compiler.PhiVars.containsKey(opr)) {
			return compiler.PhiVars.get(opr);
		}

		return false;
	}

	public void upByQT(int nowLn, String key) {

		Pair<Integer> nowP = this.QT.get(key);
		int oldLn = (Integer) nowP.getFirst();
		this.label.remove(this.label.indexOf(nowLn));
		this.UT.put("(" + nowLn + ")", "(" + oldLn + ")");

	}

	ArrayList<Integer> label = new ArrayList<Integer>();
	String title;
	int next;
	int domF = -1;
	int branchNum = 0;
	int state = 0;
	ArrayList<Node> children = new ArrayList<Node>();
	Node parent;
	Map<String, Boolean> PhiFlag = new HashMap<String, Boolean>();
	Map<String, String> PhiVar = new HashMap<String, String>();
	Map<String, Boolean> branFlag = new HashMap<String, Boolean>();
	Map<String, String> branVar = new HashMap<String, String>();
	Map<String, Pair<Integer>> QT = new HashMap<String, Pair<Integer>>();
	int whileStartLN;
	int whileEndLN;
	Map<String, String> UT = new HashMap<String, String>();
	ArrayList<String> in = new ArrayList<String>();
	ArrayList<String> out = new ArrayList<String>();
	ArrayList<Node> succ = new ArrayList<Node>();
	ArrayList<Node> pre = new ArrayList<Node>();
	String nodeFun;
}

class triples {
	String op;
	String opr1;
	String opr2;
}

class edge {
	int sourceIndex;
	int targetIndex;
}

class varID {
	public varID(String varName) {
		this.varName = varName;
		this.lastVersion = compiler.curLine;
		this.iniVersion = compiler.curLine;
	}

	public String getVar(int b) {
		Node nn = compiler.SSANodes.get(b);
		int state = nn.state;
		if (nn.PhiFlag.containsKey(this.varName)) {
			if (nn.PhiFlag.get(this.varName)) {
				return nn.PhiVar.get(this.varName);
			}
		} else if ((state == 1 || state == 2) && nn.branFlag.get(this.varName)) {
			if (nn.branVar.containsKey(this.varName)) {
				return nn.branVar.get(this.varName);
			} else {
				compiler.printError("varID.getVar");
				return null;
			}
		}
		String s = "(" + this.lastVersion + ")";
		compiler.varIndex.put(s, this);
		return s;
	}

	public String updatePhiVar(int b) {
		String s = "(" + compiler.curLine + ")";
		compiler.varIndex.put(s, this);
		return s;
	}

	public String updateVar(int b, boolean flag) {
		Node nn = compiler.SSANodes.get(b);
		int state = compiler.SSANodes.get(b).state;
		if (compiler.SSANodes.get(b).PhiFlag.containsKey(this.varName)) {
			if (compiler.SSANodes.get(b).PhiFlag.get(this.varName) == true) {
				compiler.SSANodes.get(b).PhiFlag.put(this.varName, false);
			}
		}

		if ((state == 1 || state == 2) && nn.branFlag.get(this.varName)) {
			nn.branFlag.put(this.varName, false);
		}

		if (!flag)
			this.lastVersion = compiler.curLine - 1;

		String s = "(" + this.lastVersion + ")";
		compiler.varIndex.put(s, this);
		return s;
	}

	public void addDuChain(int line, int pos) {
		this.duChain.add(new Pair<Integer>(line, pos));
	}

	public boolean DuChainContains(int line, int pos) {
		return this.duChain.contains(new Pair<Integer>(line, pos));
	}

	public boolean DuChainContainLine(int line) {
		for (Pair<Integer> x : this.duChain) {
			if (x.getFirst() == line)
				return true;
		}
		return false;
	}

	public boolean removeDuChainLine(int i) {
		for (int temp = 0; temp < this.duChain.size(); temp++) {
			Pair<Integer> p = this.duChain.get(temp);
			if (p.getFirst() == i) {
				this.duChain.remove(temp);
			}
		}
		return true;
	}

	ArrayList<Integer> varNodes = new ArrayList<Integer>();
	ArrayList<Pair<Integer>> duChain = new ArrayList<Pair<Integer>>();

	String varName;
	boolean ini = false;
	int lastVersion = compiler.curLine;
	int iniVersion = compiler.curLine;
	int phiVersion = 200;
}

class line {
	public line(String sink, String op, String opr1, String opr2) {
		this.op = op;
		this.opr1 = opr1;
		this.opr2 = opr2;
		this.sink = sink;
		if (op.equals("write"))
			this.sink = null;
	}

	public String toString() {
		return this.op + this.opr1 + this.opr2;
	}

	public String toResString() {
		return this.op + this.opr2 + this.opr1;
	}

	public String getLn() {
		return "(" + compiler.lines.indexOf(this) + ")";
	}

	public int getLNum() {
		return compiler.lines.indexOf(this);
	}

	public boolean equals(line x) {
		if (this.op.equals(x.op) && this.opr1.equals(x.opr1)) {
			boolean f2 = false;
			if (this.opr2 == null) {
				f2 = this.opr2 == x.opr2;
			} else {
				f2 = this.opr2.equals(x.opr2);
			}
			return f2;
		}

		return false;
	}

	String op;
	String opr1;
	String opr2;
	String opr3;
	String sink;
}

class Pair<T> {
	public Pair() {
		first = null;
		second = null;
	}

	public Pair(T first, T second) {
		this.first = first;
		this.second = second;
	}

	public T getFirst() {
		return first;
	}

	public T getSecond() {
		return second;
	}

	public void setFirst(T newValue) {
		first = newValue;
	}

	public void setSecond(T newValue) {
		second = newValue;
	}

	private T first;
	private T second;
}