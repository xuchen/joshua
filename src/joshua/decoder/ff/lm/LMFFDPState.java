/* This file is part of the Joshua Machine Translation System.
 * 
 * Joshua is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */

package joshua.decoder.ff.lm;

import joshua.corpus.vocab.SymbolTable;
import joshua.decoder.ff.FFDPState;


/**
 * 
 * @author Zhifei Li, <zhifei.work@gmail.com>
 * @version $LastChangedDate$
 */
public class LMFFDPState implements FFDPState {
	
	private int[] leftLMStateWords;
	private int[] rightLMStateWords;
	private String sig = null;
	
	static String SIG_PREAMBLE = "lm "; //seperator for state in signature
	static String SIG_SEP = " -S- "; //seperator for state in signature

	public  LMFFDPState() {
		//do nothing
	}
	
	 
	
	//construct an instance from the signature string
	public  LMFFDPState(SymbolTable symbolTable, String sig) {
		this.sig = sig;
		String[] states = sig.split(SIG_SEP); // TODO: use joshua.util.Regex
		this.leftLMStateWords = symbolTable.getIDs(states[0]);
		this.rightLMStateWords = symbolTable.getIDs(states[1]);
	}
		
	public void setLeftLMStateWords(int[] words_){
		this.leftLMStateWords = words_;
	}
	
	public int[] getLeftLMStateWords(){
		return this.leftLMStateWords;
	}
	
	public void setRightLMStateWords(int[] words_){
		this.rightLMStateWords = words_;
	}
	
	public int[] getRightLMStateWords(){
		return this.rightLMStateWords;
	}

	public String getSignature(boolean forceRecompute) {
		return getSignature(null, forceRecompute);
	}
	
	/* BUG: now, the getSignature is also got called by diskgraph; 
	 * this may change the this.sig from integers to strings
	 * */
	public String getSignature(SymbolTable symbolTable, boolean forceRecompute) {
		if (forceRecompute || sig == null) {
			StringBuffer sb = new StringBuffer();
			//sb.append(SIG_PREAMBLE);//TODO: do we really need this
			
			/**we can not simply use sb.append(leftLMStateWords), 
			 * as it will just add the address of leftLMStateWords.
			 */
			computeStateSig(symbolTable, leftLMStateWords, sb); 
			
			sb.append(SIG_SEP);//TODO: do we really need this
			
			computeStateSig(symbolTable, rightLMStateWords, sb);
			
			this.sig = sb.toString();
		}
		//System.out.println("lm sig is:" + this.sig);
		return this.sig;
	}
	
	
	
	private void computeStateSig(SymbolTable symbolTable, int[] state, StringBuffer sb) {
		
		if (null != state) {
			for (int i = 0; i < state.length; i++) {
				if (true
					//TODO: equivalnce: number of <null> or <bo>?
					/* states[i]!=Symbol.NULL_RIGHT_LM_STATE_SYM_ID
					 * && states[i]!=Symbol.NULL_LEFT_LM_STATE_SYM_ID
					 * && states[i]!=Symbol.LM_STATE_OVERLAP_SYM_ID*/
				) {
					if (null != symbolTable) {
						sb.append(symbolTable.getWord(state[i]));
					} else {
						sb.append(state[i]);
					}
					if (i < state.length - 1) {
						sb.append(' ');
					}
				}
			}
		} else {
			throw new RuntimeException("state is null");
		}
	}

}
