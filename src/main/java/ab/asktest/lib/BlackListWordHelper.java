package ab.asktest.lib;

import java.util.HashMap;
import java.util.List;

import ab.asktest.dao.obj.WordBlackList;

/**
 * BlackListWordHelper
 * 
 * Helper class to hold a collection of all blacklisted words.
 * 
 * Note: This current version does not filter based on the locale of the user.
 * A second version of this service would try to be more clever about how this is done though.
 * Needs some whiteboard time.
 * 
 * Future versions of this would probably have a tree per character set/alphabet rather than
 * per language/locale? Cursewords tend to jump between languages I think, but not alphabets as such.
 * Also I would expect different instance deployments/clusters of the service per geography/alphabet I guess, 
 * so each instance would only support one alphabet/set of blacklisted words? 
 * 
 * TODO: will need extensive testing for speed and memory usage over time. 
 * Will hold a tree in memory so there are some considerations there as well - it will 
 * sit in a static variable and not be GC'd. Also, as the structure is created at init() then
 * we should not have any fragmentation issues.   
 * 
 * This is a crude first effort. Users will add non printable characters, 
 * different alphabets, numbers, letters to the start of the bad word, etc to get around this filter. 
 * 
 * Note: It would also be the responsibility for the blacklist creator to make sure to populate it 
 * with lots of stem words, etc - will make for faster searches and a smaller tree in memory 
 *  
 * @author andrewb
 * @version 0.0.1
 * */
public class BlackListWordHelper {

	private HashMap<Character, TreeNode> dictionary;
	
	public BlackListWordHelper(List<WordBlackList> wordList){
		//build the dictionary
		dictionary = new HashMap<>();
		
		TreeNode tmp;
		for(WordBlackList word:wordList){
			if(word.getWord()==null||word.getWord().length()<1){
				continue;
			}
			char ws[] = word.getWord().toLowerCase().toCharArray();
			tmp = dictionary.get(ws[0]);
			if(tmp==null){
				tmp = new TreeNode(ws.length==1, ws[0]);
				dictionary.put(ws[0], tmp);
			}
			tmp.addToTree(1, ws);			
		}
	}
	/**
	 * questionContainsBlackListedWord
	 * assumption: space delimited line of text
	 * @param String question. assumption: space delimited line of text
	 * @return boolean if question contained a blacklisted word or not
	 * */
	public boolean questionContainsBlackListedWord(String question){
		if(question==null||question.length()<1){
			return false;
		}		
		
		for(String s:question.split(" ")){
			if(wordIsBlackListed(s)){
				return true;
			}			
		}
		
		return false;
	}
	/**
	 * wordIsBlackListed
	 * @param String word. ASSUMPTION: single word with *no* space chars
	 * @return boolean if word is blacklisted or not
	 * */
	public boolean wordIsBlackListed(String word){
		if(word==null||word.length()<1){
			return false;
		}
		//remove non printable chars - TODO - needs testing
		//honesty note: reg-ex stolen from stack overflow :)		
		word = word.toLowerCase().replaceAll("[\\x00\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
		char wordArr[] = word.toCharArray();
		TreeNode tmp = dictionary.get(wordArr[0]);
		if(tmp!=null && tmp.isFound(0, wordArr)){
			 return true;
		}
		return false;
	}
	/**
	 * Tree structure modelling the dictionary of blacklisted words. 
	 * Each node contains a char, and has children.
	 * Each node is also the last letter of a word or not.
	 * 
	 * Using the Java String contains() methods or a clever reg-ex will still necessitate a full
	 * scan of the string for *every* blacklisted word in our DB. Trying to be more clever here.   
	 * 
	 * A tree search should take O(n) here - where n is the length of each inputed word in the users question.
	 * Its more expensive to build and hold the tree initially, but that happens on startup rather than 
	 * when the user is blocking on submitting a question.
	 * 
	 * Note: Have not addressed periodically rebuilding the tree from the datasource. This could run
	 * on a Quartz/other timer internally every few hours/days?
	 * */
	class TreeNode{
		
		private boolean wordEnd;
		private char charachter;		
		private HashMap<Character, TreeNode> children;
		
		public TreeNode(boolean wordEnd, char charachter) {
			super();
			this.wordEnd = wordEnd;
			this.charachter = charachter;
			this.children = new HashMap<>();
		}
		public boolean isFound(int place, char word[]){
			if( charachter==word[place] ){
				if(wordEnd){
					//last char matched and this is the end of the stored word. we're done!
					//note: doesn't matter if the *inputted* word is finished or not 
					//	- we have found a blacklisted word contained in it
					return true;
				}else if(children.isEmpty()){
					return false;
				}else{
					//peek at the next char
					place++;
					if(place>=word.length){
						return false;
					}
					TreeNode child = children.get(word[place]);
					if(child==null){
						return false;
					}												
					return child.isFound(place, word);
				}							
			}else{
				return false;
			}
		}
		public void addToTree(int position, char word[]){	//t r e e
			if(position>=word.length){
				return;
			}
			char c = word[position];
			TreeNode child = children.get(c);
			if(child==null){//add it				
				child = new TreeNode(position==(word.length-1), c);
				children.put(c, child);
			}
			position++;
			child.addToTree(position, word);
		}
		@Override
		public String toString() {
			return "TreeNode [wordEnd=" + wordEnd + ", charachter="
					+ charachter + ", children("+(children.size())+")=\n\t" + children + "]";
		}		
	}
	
	
}
