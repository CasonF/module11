package application;

public class Word {
	
	public String word;
	int occurrence, id;
	
	public Word(String word, int occurrence, int id)
	{
		this.word = word;
		this.occurrence = occurrence;
		this.id = id;
	}
	
	public Word(String word, int occurrence)
	{
		this.word = word;
		this.occurrence = occurrence;
	}
	
	public Word(String word)
	{
		this.word = word;
	}
	
	public Word(int id)
	{
		this.id = id;
	}
	
	public Word() {}

}
