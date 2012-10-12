package org.aksw.simba.rdflivenews.pattern.similarity.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.similarity.SimilarityMetric;
import org.aksw.simba.rdflivenews.wordnet.Wordnet;

import com.github.gerbsen.maven.MavenUtil;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 * 
 * Originally coded in Python from TakeLab.
 * Project web page: http://takelab.fer.hr/sts/
 */
public class TakelabSimilarityMetric implements SimilarityMetric {

	@Override
	public double calculateSimilarity(Pattern pattern1, Pattern pattern2) {
		
		return get_similarity(pattern1.getNaturalLanguageRepresentation(),
				pattern2.getNaturalLanguageRepresentation());
		
	}

	private double get_similarity(String sentence1, String sentence2) {
	
		ArrayList<ArrayList<String>> s1 = build_array_lists(sentence1);
		ArrayList<ArrayList<String>> s2 = build_array_lists(sentence2);
		Double[] f = calc_features(s1, s2);
		double sim = 0.0;
		
		// TODO Replace them with the weights learned from annotation.
		double[] w = { 1, 1, 1, 0.285714, 0.457207, 0.267711, 0.254553,
				0.110239, 0.249999, 0.115564 };
//		System.out.println("\n" + sentence1 + " | " + sentence2);
		for (int i = 0; i < f.length; i++) {
			sim += f[i] * w[i];
//			System.out.println(sim_name[i] + "\t" + f[i] + " * " + w[i]);
		}
		double bias = 4.625891026454163;
		sim = sim / bias;
		if (sim > 1.0)
			sim = 1.0;
		return sim;
	}

	private String[] stopwords = { "i", "a", "about", "an", "are", "as", "at",
			"be", "by", "for", "from", "how", "in", "is", "it", "of", "on",
			"or", "that", "the", "this", "to", "was", "what", "when", "where",
			"who", "will", "with", "the", "'s", "did", "have", "has", "had",
			"were", "'ll" };

	private HashMap<Tuple<String, String>, Double> wpathsimcache = new HashMap<Tuple<String, String>, Double>();

	private Wordnet wordnet = Wordnet.getInstance();

	private HashMap<String, Double> wweight;
	private double minwweight;

	private Sim nyt_sim;
	// private Sim wiki_sim = new Sim("wikipedia_words.txt",
	// 		"wikipedia_word_vectors.txt");

	private String[] sim_name = { "nfeat", "cmtch", "smtch", "ngram", "wnsim",
			"wword", "nytds", "nytwd", "rldif", "ridif" };

	public TakelabSimilarityMetric() {

	    try {
	    
	        wweight = load_wweight_table();
	        nyt_sim = new Sim();
	        minwweight = min_wweight();
	    }
	    catch (Exception e) {
	        
	        throw new RuntimeException("Bad thing happend! Training files not found in ", e);
	    }
	}

	private double min_wweight() {
		double min = Double.MAX_VALUE;
		for (Double d : wweight.values())
			if (d < min)
				min = d;
		return min;
	}

	@SuppressWarnings("unused")
	private String[] fix_compounds(String[] a, String[] b) {

		String[] sb = new String[b.length];
		for (int i = 0; i < b.length; i++)
			sb[i] = b[i].toLowerCase();

		String[] a_fix = new String[a.length];
		int la = a.length;
		int i = 0, j = 0;
		while (i < la) {
			if (i + 1 < la) {
				String comb = a[i] + a[i + 1];
				for (String sbi : sb) {
					if (sbi.equals(comb.toLowerCase())) {
						a_fix[j] = comb;
						i += 2;
						break;
					}
				}
			}
			a_fix[j] = a[i];
			i += 1;
			j += 1;
		}
		return a_fix;
	}

	private String clean_string(String l) {
		l = l.replaceAll("’", "'");
		l = l.replaceAll("``", "\"");
		l = l.replaceAll("''", "\"");
		l = l.replaceAll("—", "--");
		l = l.replaceAll("–", "--");
		l = l.replaceAll("´", "'");
		l = l.replaceAll("-", " ");
		l = l.replaceAll("/", " ");

		if (l.equals("n't"))
			l = "not";
		if (l.equals("'m"))
			l = "am";

		return l.trim();
	}

	private ArrayList<ArrayList<String>> build_array_lists(String sentence) {
		ArrayList<ArrayList<String>> res = new ArrayList<>();
		String[] words = sentence.split(" ");
		for (String w : words) {
			String[] parts = w.split("_");
			ArrayList<String> arr = new ArrayList<>();
			String s = clean_string(parts[0]);
			if (!s.equals("")) {
				arr.add(s);
				res.add(arr);
			}
		}
		return res;
	}

	private Double get_wweight_of(String s) {
		Double d = wweight.get(s);
		if (d == null)
			return 0.0;
		else
			return d;
	}

	private HashMap<String, Double> load_wweight_table()
			throws FileNotFoundException {
		HashMap<String, Double> ww = new HashMap<>();
		Scanner fs = new Scanner(MavenUtil.loadFile("/similarity/word-frequencies.txt"));
		double totfreq = 0;
		if (fs.hasNextLine()) {
			totfreq = Double.parseDouble(fs.nextLine());
		} else {
			fs.close();
			return ww;
		}
		while (fs.hasNextLine()) {
			String[] l = fs.nextLine().split(" ");

			int freq = Integer.parseInt(l[1]);
			if (freq >= 10)
				ww.put(l[0], Math.log(totfreq / (double) freq));
		}
		fs.close();
		return ww;
	}

	private double len_compress(int l) {
		return Math.log(1. + l);
	}

	private boolean is_word(String w) {
		String regexp = "[^0-9,.(=)\\[\\]/_`]+$";
		return w.matches(regexp);
	}

	private ArrayList<String> get_locase_words(ArrayList<ArrayList<String>> spos) {
		ArrayList<String> t = new ArrayList<String>();
		for (ArrayList<String> s : spos)
			if (is_word(s.get(0))) {
				t.add(s.get(0).toLowerCase());
			}
		return t;
	}

	/*
	 * The original method was worth to compare words only. It always returned
	 * 1.0 when comparing sentences, i.e. arrays. This variant builds ngrams
	 * also for the whole sentence.
	 */
	private ArrayList<String> make_ngrams(ArrayList<String> sa, int n) {
		ArrayList<String> rez = new ArrayList<String>();
		for (String s : sa) {
			for (int i = 0; i < -n + s.length() + 1; i++)
				rez.add(s.substring(i, i + n));
			rez.add(s.substring(n - 1));
		}
		return rez;
	}

	private double dist_sim(Sim sim, ArrayList<String> lema,
			ArrayList<String> lemb) {
		HashMap<String, Integer> wa = counter(lema);
		HashMap<String, Integer> wb = counter(lemb);
		HashMap<String, Double> d1 = new HashMap<>();
		HashMap<String, Double> d2 = new HashMap<>();
		for (String a : wa.keySet())
			d1.put(a, 1.0);
		for (String b : wb.keySet())
			d2.put(b, 1.0);
		return sim.calc(d1, d2);
	}

	private double weighted_dist_sim(Sim sim, ArrayList<String> lca,
			ArrayList<String> lcb) {
		HashMap<String, Integer> wa = counter(lca);
		HashMap<String, Integer> wb = counter(lcb);
		HashMap<String, Double> d1 = new HashMap<>();
		HashMap<String, Double> d2 = new HashMap<>();
		for (String x : wa.keySet())
			d1.put(x, get_wweight_of(x) * wa.get(x));
		for (String x : wb.keySet())
			d2.put(x, get_wweight_of(x) * wb.get(x));
		return sim.calc(d1, d2);
	}

	private double weighted_word_match(ArrayList<String> olca,
			ArrayList<String> olcb) {
		HashMap<String, Integer> wa = counter(olca);
		HashMap<String, Integer> wb = counter(olcb);

		double wsuma = 0, wsumb = 0;
		for (String w : wa.keySet())
			wsuma += get_wweight_of(w) * wa.get(w);
		for (String w : wb.keySet())
			wsumb += get_wweight_of(w) * wb.get(w);

		double wsum = 0.0;
		for (String w : wa.keySet()) {
			int wva = 0;
			try {
				wva = wa.get(w);
			} catch (NullPointerException e) {
			}
			int wvb = 0;
			try {
				wvb = wb.get(w);
			} catch (NullPointerException e) {
			}
			wsum += get_wweight_of(w) * Math.min(wva, wvb);
		}
		double p = 0, r = 0;
		if (wsuma > 0 && wsum > 0)
			p = (double) wsum / (double) wsuma;
		if (wsumb > 0 && wsum > 0)
			r = (double) wsum / (double) wsumb;
		double f1 = (p + r > 0) ? 2 * p * r / (p + r) : 0;
		return f1;
	}

	private HashMap<String, Integer> counter(ArrayList<String> arr) {
		HashMap<String, Integer> wa = new HashMap<>();
		for (String a : arr)
			if (wa.containsKey(a)) {
				int v = wa.get(a).intValue();
				wa.remove(a);
				wa.put(a, v + 1);
			} else {
				wa.put(a, 1);
			}
		return wa;
	}

	private double wpathsim(String a, String b) {

		if (a.compareTo(b) > 0) {
			String temp = a;
			a = b;
			b = temp;
		}
		Tuple<String, String> p = new Tuple<String, String>(a, b);
		if (wpathsimcache.containsKey(p))
			return wpathsimcache.get(p);
		if (a.equals(b)) {
			wpathsimcache.remove(p);
			wpathsimcache.put(p, 1.0);
			return 1.0;
		}

		/*
		 * The JAWS Library doesn't provide a getter for the synset unique ID! A
		 * solution is to skip the load of the synsets and call directly the
		 * similarity among words.
		 * 
		 * In the original implementation the overall similarity was the maximum
		 * similarity of the synset pairs.
		 */
		double ps = wordnet.getWordnetSimilarity(a, b, Wordnet.PATH_SIMILARITY);
		wpathsimcache.remove(p);
		wpathsimcache.put(p, ps);
		return ps;
	}

	private double calc_wn_prec(ArrayList<String> lema, ArrayList<String> lemb) {
		double rez = 0;
		for (String a : lema) {
			double ms = 0;
			for (String b : lemb)
				ms = Math.max(ms, wpathsim(a, b));
			rez += ms;
		}
		return rez / (double) lema.size();
	}

	private double wn_sim_match(ArrayList<String> lema, ArrayList<String> lemb) {
		double f1 = 1.0, p = 0.0, r = 0.0;

		if (lema.size() > 0 && lemb.size() > 0) {
			p = calc_wn_prec(lema, lemb);
			r = calc_wn_prec(lemb, lema);
			f1 = (p + r > 0) ? 2. * p * r / (p + r) : 0.0;
		}
		return f1;
	}

	private double ngram_match(ArrayList<String> sa, ArrayList<String> sb, int n) {
		ArrayList<String> nga = make_ngrams(sa, n);
		ArrayList<String> ngb = make_ngrams(sb, n);
		int matches = 0;
		HashMap<String, Integer> c1 = counter(nga);
		for (String ng : ngb) {
			if (c1.get(ng) != null) {
				Integer v = c1.get(ng);
				if (v > 0) {
					c1.remove(ng);
					c1.put(ng, v - 1);
					matches++;
				}
			}
		}
		double p = 0.0, r = 0.0, f1 = 1.0;
		if (nga.size() > 0 && ngb.size() > 0) {
			p = (double) (matches) / (double) nga.size();
			r = (double) (matches) / (double) ngb.size();
			f1 = (p + r > 0) ? 2 * p * r / (p + r) : 0.0;
		}
		return f1;
	}

	// private SynsetType to_wordnet_tag(String key) {
	// switch(key) {
	// case "NN": return SynsetType.NOUN; //wordnet.NOUN;
	// case "JJ": return SynsetType.ADJECTIVE; //wordnet.ADJ;
	// case "VBD": return SynsetType.VERB; //wordnet.VERB;
	// case "RB": return SynsetType.ADVERB; //wordnet.ADV;
	// default: return null;
	// }
	// }

	/*
	 * Used only to filter the stopwords out.
	 */
	private ArrayList<String> get_lemmatized_words(
			ArrayList<ArrayList<String>> sa) {
		ArrayList<String> rez = new ArrayList<String>();
		for (ArrayList<String> wwpos : sa) {
			String w = wwpos.get(0).toLowerCase();
			// String wpos = wwpos.get(1);
			if (!isIn(w, stopwords) && is_word(w)) {
				String wlem = null;
				// SynsetType wtag = to_wordnet_tag(wpos);
				// Original code was:
				// if(wtag == null)
				// wlem = w;
				// else {
				// wlem = wordnet.morphy(w, wtag);
				// but our words are already lemmas (i.e., at their canonical
				// form).
				// if(wlem == null)
				wlem = w;
				// }
				rez.add(wlem);
			}
		}
		return rez;
	}

	private boolean is_stock_tick(String w) {
		return w.charAt(0) == '.' && w.length() > 1 && isupper(w.substring(1));
	}

	private boolean isupper(String w) {
		for (int i = 0; i < w.length(); i++)
			if (!Character.isUpperCase(w.charAt(i)))
				return false;
		return true;
	}

	private double[] stocks_matches(ArrayList<ArrayList<String>> sa,
			ArrayList<ArrayList<String>> sb) {
		ArrayList<String> ca = new ArrayList<>();
		for (int i = 1; i < sa.size(); i++) {
			ArrayList<String> x = sa.get(i);
			String s = x.get(0);
			if (is_stock_tick(s))
				ca.add(s);
		}
		ArrayList<String> cb = new ArrayList<>();
		ArrayList<String> cacb = new ArrayList<>();
		for (int i = 1; i < sb.size(); i++) {
			ArrayList<String> x = sb.get(i);
			String s = x.get(0);
			if (is_stock_tick(s)) {
				cb.add(s);
				if (ca.contains(s))
					cacb.add(s);
			}
		}

		int la = ca.size();
		int lb = cb.size();
		int isect = cacb.size();

		double f = 1.0, p = 0.0, r = 0.0;
		if (la > 0 && lb > 0) {
			if (isect > 0) {
				p = (double) isect / (double) la;
				r = (double) isect / (double) lb;
				f = 2.0 * p * r / (p + r);
			} else {
				f = 0.0;
			}
		}

		double[] res = { len_compress(la + lb), f };
		return res;
	}

	private double[] case_matches(ArrayList<ArrayList<String>> sa,
			ArrayList<ArrayList<String>> sb) {
		ArrayList<String> ca = new ArrayList<>();
		for (int i = 1; i < sa.size(); i++) {
			ArrayList<String> x = sa.get(i);
			String s = x.get(0);
			if (Character.isUpperCase(s.charAt(0))
					&& s.charAt(s.length() - 1) != '.')
				ca.add(s);
		}
		ArrayList<String> cb = new ArrayList<>();
		ArrayList<String> cacb = new ArrayList<>();
		for (int i = 1; i < sb.size(); i++) {
			ArrayList<String> x = sb.get(i);
			String s = x.get(0);
			if (Character.isUpperCase(s.charAt(0))
					&& s.charAt(s.length() - 1) != '.') {
				cb.add(s);
				if (ca.contains(s))
					cacb.add(s);
			}
		}

		int la = ca.size();
		int lb = cb.size();
		int isect = cacb.size();

		double f = 1.0, p = 0.0, r = 0.0;
		if (la > 0 && lb > 0) {
			if (isect > 0) {
				p = (double) isect / (double) la;
				r = (double) isect / (double) lb;
				f = 2.0 * p * r / (p + r);
			} else {
				f = 0.0;
			}
		}

		double[] res = { len_compress(la + lb), f };
		return res;
	}

	private boolean match_number(String xa, String xb) {
		if (xa.equals(xb))
			return true;
		xa = xa.replace(",", "");
		xb = xb.replace(",", "");

		try {
			int va = (int) (Double.parseDouble(xa));
			int vb = (int) (Double.parseDouble(xb));
			if ((va == 0 || vb == 0) && va != vb)
				return false;
			double fxa = Double.parseDouble(xa);
			double fxb = Double.parseDouble(xb);
			if (Math.abs(fxa - fxb) > 1)
				return false;

			int diga = xa.indexOf('.');
			int digb = xb.indexOf('.');

			diga = (diga == -1) ? 0 : xa.length() - diga - 1;
			digb = (digb == -1) ? 0 : xb.length() - digb - 1;
			if (diga > 0 && digb > 0 && va != vb)
				return false;
			int dmin = Math.min(diga, digb);
			if (dmin == 0) {
				if (Math.abs(Math.round(fxa) - Math.round(fxb)) < 1e-5)
					return true;
				return va == vb;
			}
			return Math.abs(round(fxa, dmin) - round(fxb, dmin)) < 1e-5;

		} catch (Exception e) {
		}

		return false;
	}

	/**
	 * Return the floating point value x rounded to n digits after the decimal
	 * point. If n is omitted, it defaults to zero. The result is a floating
	 * point number. Values are rounded to the closest multiple of 10 to the
	 * power minus n; if two multiples are equally close, rounding is done away
	 * from 0 (so. for example, round(0.5) is 1.0 and round(-0.5) is -1.0).
	 * 
	 * @param x
	 * @param n
	 * @return
	 */
	private double round(double x, int n) {
		double p = Math.pow(10, n);
		return Math.round(x / p) * p;
	}

	private double[] number_features(ArrayList<ArrayList<String>> sa,
			ArrayList<ArrayList<String>> sb) {

		ArrayList<String> numa = new ArrayList<>();
		ArrayList<String> numb = new ArrayList<>();

		String risnum = "^[0-9,./-]+$";
		String rhasdigit = "[0-9]";
		for (ArrayList<String> x : sa) {
			String s = x.get(0);
			if (s.matches(risnum) && s.matches(rhasdigit))
				numa.add(s);
		}
		for (ArrayList<String> x : sb) {
			String s = x.get(0);
			if (s.matches(risnum) && s.matches(rhasdigit))
				numb.add(s);
		}
		int isect = 0;
		for (String na : numa) {
			if (numb.contains(na)) {
				isect++;
			} else {
				for (String nb : numb) {
					if (match_number(na, nb)) {
						isect++;
						break;
					}
				}
			}
		}

		int la = numa.size(), lb = numb.size();
		double f = 1.0, subset = 0.0, p = 0.0, r = 0.0;
		if (la + lb > 0) {
			if (isect == la || isect == lb)
				subset = 1.0;
			if (isect > 0) {
				p = (double) isect / (double) la;
				r = (double) isect / (double) lb;
				f = 2.0 * p * r / (p + r);
			} else {
				f = 0.0;
			}
		}
		double[] res = { len_compress(la + lb), f, subset };
		return res;
	}

	private double relative_len_difference(ArrayList<String> lca,
			ArrayList<String> lcb) {
		double la = lca.size();
		double lb = lcb.size();
		return 1.0 - Math.abs(la - lb) / Math.max(la, lb);
	}

	private double relative_ic_difference(ArrayList<String> lca,
			ArrayList<String> lcb) {
		double wa = 0.0, wb = 0.0;
		for (String x : lca)
			wa += Math.max(0.0, get_wweight_of(x) - minwweight);
		for (String x : lcb)
			wb += Math.max(0.0, get_wweight_of(x) - minwweight);
		return 1.0 - Math.abs(wa - wb) / Math.max(wa, wb);
	}

	private boolean isIn(String what, String[] where) {
		for (String s : where)
			if (s.equals(what))
				return true;
		return false;
	}

	private Double[] calc_features(ArrayList<ArrayList<String>> sa,
			ArrayList<ArrayList<String>> sb) {
		ArrayList<String> olca = get_locase_words(sa);
		ArrayList<String> olcb = get_locase_words(sb);
		ArrayList<String> lca = new ArrayList<String>();
		for (String w : olca)
			if (!isIn(w, stopwords))
				lca.add(w);
		ArrayList<String> lcb = new ArrayList<String>();
		for (String w : olcb)
			if (!isIn(w, stopwords))
				lcb.add(w);
		ArrayList<String> lema = get_lemmatized_words(sa);
		ArrayList<String> lemb = get_lemmatized_words(sb);

		double[] n_features = number_features(sa, sb);
		double[] c_matches = case_matches(sa, sb);
		double[] s_matches = stocks_matches(sa, sb);
		Double[] f = {
				// n_features[0],
				n_features[1],
				// n_features[2],
				// c_matches[0],
				c_matches[1],
				// s_matches[0],
				s_matches[1],
				ngram_match(lca, lcb, 1),
				// ngram_match(lca, lcb, 2),
				// ngram_match(lca, lcb, 3),
				// we don't need these as the words are already lemmas.
				// ngram_match(lema, lemb, 1),
				// ngram_match(lema, lemb, 2),
				// ngram_match(lema, lemb, 3),
				wn_sim_match(lema, lemb),
				weighted_word_match(olca, olcb),
				// as above.
				// weighted_word_match(lema, lemb),
				sa.equals(sb) ? 1.0 : dist_sim(nyt_sim, lema, lemb),
				sa.equals(sb) ? 1.0 : weighted_dist_sim(nyt_sim, lema, lemb),
				// too heavy to load.
				// weighted_dist_sim(wiki_sim, lema, lemb),
				relative_len_difference(lca, lcb),
				relative_ic_difference(olca, olcb) };

		return f;
	}

	private class Sim {

		private HashMap<String, Integer> word_to_idx = new HashMap<>();
		private double[][] mat;

		private Sim() throws FileNotFoundException {
			long t = System.currentTimeMillis();
			Scanner fs = new Scanner(MavenUtil.loadFile("/similarity/nyt_words.txt"));
			for (int i = 0; fs.hasNextLine(); i++)
				word_to_idx.put(fs.nextLine().trim(), i);
			fs.close();

			fs = new Scanner(MavenUtil.loadFile("/similarity/nyt_word_vectors.txt"));
			ArrayList<String[]> lines = new ArrayList<>();
			String[] l = new String[0];
			while (fs.hasNextLine()) {
				l = fs.nextLine().split(" ");
				lines.add(l);
			}
			fs.close();
			mat = new double[lines.size()][l.length];
			// System.out.println("["+lines.size()+"]["+l.length+"]");
			for (int i = 0; i < mat.length; i++)
				for (int j = 0; j < mat[i].length; j++)
					mat[i][j] = Double.parseDouble(lines.get(i)[j]);

			double sec = (System.currentTimeMillis() - t) / 1000.0;
			System.out.println("Word vectors from nyt_word_vectors.txt loaded in "
					+ sec + " sec.");

		}

		private double[] bow_vec(HashMap<String, Double> b) {
			double[] vec = new double[lengthOneOf(mat)];
			for (String k : b.keySet()) {
				Double v = b.get(k);
				Integer idx = word_to_idx.get(k);
				if (idx == null)
					idx = -1;
				if (idx >= 0)
					for (int i = 0; i < vec.length; i++)
						vec[i] += mat[idx][i] / (norm(mat[idx]) + 1e-8) * v;
			}
			return vec;
		}

		private int lengthOneOf(double[][] matrix) {
			int len;
			try {
				len = matrix[0].length;
			} catch (ArrayIndexOutOfBoundsException e) {
				len = 0;
			}
			return len;
		}

		private double calc(HashMap<String, Double> b1,
				HashMap<String, Double> b2) {
			double[] v1 = bow_vec(b1);
			double[] v2 = bow_vec(b2);
			double dot = 0.0;
			for (int i = 0; i < v1.length; i++)
				dot += v1[i] * v2[i];
			double a = norm(v1) + 1e-8;
			double b = norm(v2) + 1e-8;
			return Math.abs(dot / (a * b));
		}

		private double norm(double[] v1) {
			double sum = 0.0;
			for (double v : v1)
				sum += Math.pow(v, 2);
			return Math.sqrt(sum);
		}
	}

	private class Tuple<X, Y> implements Comparable<Tuple<X, Y>> {
		public final X x;
		public final Y y;

		public Tuple(X x, Y y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public int compareTo(Tuple<X, Y> arg0) {
			if (x.equals(arg0.x) && y.equals(arg0.y))
				return 0;
			else
				return 1;
		}

	}
}
