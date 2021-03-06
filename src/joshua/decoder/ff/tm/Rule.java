package joshua.decoder.ff.tm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import joshua.corpus.Vocabulary;
import joshua.decoder.ff.FeatureFunction;
import joshua.decoder.ff.FeatureVector;

/**
 * This class define the interface for Rule. Normally, the feature score in the rule should be
 * *cost* (i.e., -LogP), so that the feature weight should be positive.
 * 
 * @author Zhifei Li, <zhifei.work@gmail.com>
 */
public abstract class Rule {

  // ===============================================================
  // Attributes
  // ===============================================================

  public abstract void setArity(int arity);

  public abstract int getArity();

  public abstract void setOwner(int ow);

  public abstract int getOwner();

  public abstract void setLHS(int lhs);

  public abstract int getLHS();

  public abstract void setEnglish(int[] eng);

  public abstract int[] getEnglish();

  /**
   * The nonterminals on the English side are pointers to the source side nonterminals (-1 and -2),
   * rather than being directly encoded. These number indicate the correspondence between the
   * nonterminals on each side, introducing a level of indirection however when we want to resolve
   * them. So to get the ID, we need to look up the corresponding source side ID.
   * 
   * @return The string of English words
   */
  public String getEnglishWords() {
    List<String> foreignNTs = getForeignNonTerminals();

    StringBuilder sb = new StringBuilder();
    for (Integer index : getEnglish()) {
      if (index >= 0)
        sb.append(Vocabulary.word(index) + " ");
      else
        sb.append(foreignNTs.get(foreignNonTerminalIndexForEnglishIndex(index)) + ","
            + Math.abs(index) + " ");
    }

    return sb.toString().trim();
  }

  public boolean isTerminal() {
    for (int i = 0; i < getEnglish().length; i++)
      if (getEnglish()[i] < 0)
        return false;
    
    return true;
  }
  
  /**
   * Return the French (source) nonterminals as list of Strings
   * @return
   */
  public List<String> getForeignNonTerminals() {
    List<String> foreignNTs = new ArrayList<String>();
    for (int i = 0; i < getFrench().length; i++) {
      if (getFrench()[i] < 0)
        foreignNTs.add(Vocabulary.word(getFrench()[i]));
    }
    return foreignNTs;
  }
  
  /**
   * Return the English (target) nonterminals as list of Strings
   * @return
   */
  List<String> getEnglishNonTerminals() {
    List<String> result = new ArrayList<String>();
    List<String> foreignNTs = getForeignNonTerminals();

    // for (int i = 0; i < this.getEnglish().length; i++) {
    for (Integer index : getEnglish()) {
      if (index < 0) {
        result.add(foreignNTs.get(foreignNonTerminalIndexForEnglishIndex(index)) + ","
            + Math.abs(index));
      }
    }

    return result;
  }
  
  private static int foreignNonTerminalIndexForEnglishIndex(int index) {
    return Math.abs(index) - 1;
  }
  
  private List<Integer> getNormalizedEnglishNonterminalIndices() {
    List<Integer> result = new ArrayList<Integer>();

    for (Integer index : getEnglish()) {
      if (index < 0) {
        result.add(foreignNonTerminalIndexForEnglishIndex(index));
      }
    }

    return result;
  }
  
  public boolean isInverting() {
    List<Integer> normalizedEnglishNonTerminalIndices = getNormalizedEnglishNonterminalIndices();
    if (normalizedEnglishNonTerminalIndices.size() == 2) {
      if (normalizedEnglishNonTerminalIndices.get(0) == 1) {
        return true;
      }
    }
    return false;
  }

  public abstract void setFrench(int[] french);

  public abstract int[] getFrench();

  public abstract String getFrenchWords();

  /**
   * This function returns the dense (phrasal) features discovered when the rule was loaded. Dense
   * features are the list of unlabeled features that preceded labeled ones. They can also be
   * specified as labeled features of the form "tm_OWNER_INDEX", but the former format is preferred.
   */
  public abstract FeatureVector getFeatureVector();

  /**
   * This allows the estimated cost of a rule to be applied from the outside.
   * 
   * @param cost
   */
  public abstract void setEstimatedCost(float cost);

  /**
   * This function is called by the rule comparator when sorting the grammar. As such it may be
   * called many times and any implementation of it should be a cached implementation.
   * 
   * @return the estimated cost of the rule (a lower bound on the true cost)
   */
  public abstract float getEstimatedCost();

  /**
   * Precomputable costs is the inner product of the weights found on each grammar rule and the
   * weight vector. This is slightly different from the estimated rule cost, which can include other
   * features (such as a language model estimate). This getter and setter should also be cached, and
   * is basically provided to allow the PhraseModel feature to cache its (expensive) computation for
   * each rule.
   * 
   * @return the precomputable cost of each rule
   */
  public abstract float getPrecomputableCost();

  public abstract void setPrecomputableCost(float cost);

  // ===============================================================
  // Methods
  // ===============================================================

  /**
   * Set a lower-bound estimate inside the rule returns full estimate.
   */
  public abstract float estimateRuleCost(List<FeatureFunction> models);

  /**
   * This comparator is used for sorting during cube pruning. It sorts items in reverse.
   */
  public static Comparator<Rule> NegativeCostComparator = new Comparator<Rule>() {
    public int compare(Rule rule1, Rule rule2) {
      float cost1 = rule1.getEstimatedCost();
      float cost2 = rule2.getEstimatedCost();
      if (cost1 > cost2) {
        return -1;
      } else if (cost1 == cost2) {
        return 0;
      } else {
        return 1;
      }
    }
  };
}
