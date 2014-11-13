package taojava.util;

import java.util.Iterator;
import java.util.Random;

/**
 * A randomized implementation of sorted lists.  
 * 
 * @author Samuel A. Rebelsky
 * @author Albert Owusu-Asare
 */
public class SkipList<T extends Comparable<T>>
    implements SortedList<T>
{
  // +--------+----------------------------------------------------------
  // | Fields |
  // +--------+
  
  int currentLevel; // the level  of the node
  int size; //size of the list
  double probability;
  int MAX_LEVEL;  //the level of the list
  Node<T> header;  // the front of the list stores references to all the 
  //different levels from 1 to MaxLevel
  
  // +------------------+------------------------------------------------
  // | Internal Classes |
  // +------------------+

  /**
   * Nodes for skip lists.
   */
  public class Node <T> 
  {
    // +--------+--------------------------------------------------------
    // | Fields |
    // +--------+

    /**
     * The value stored in the node.
     */
    T val;
    
    Node <T> [] forwardPointers;
    
    // +--------------+----------------------------------------------------
    // | Constructors |
    // +--------------+
    /**
     * Constructs a new node
     * @param nodeLevel the level this node is at.
     * @param data the data to be stored in the node.
     */
    @SuppressWarnings("unchecked")
    public Node(int nodeLevel,T data){
      this.val = data; 
      // we will use index 1 to nodeLevel for our forward pointers
      this.forwardPointers = new Node[nodeLevel+1]; 
      //initialize all the pointers to null upon creation
      for(Node<T> nodeRef : forwardPointers){
        nodeRef = null;
      }//for   
    }//Node(int, T) 
  } // class Node

  
  // +--------------+----------------------------------------------------
  // | Constructors |
  // +--------------+
  /**
   * Constructs a new SkipList 
   * @param maxLevel the maximum number of levels in the skip List
   */
  @SuppressWarnings("unchecked")
  public SkipList(int maxLevel){
    this.currentLevel = 1 ;
    this.MAX_LEVEL =  maxLevel;
    this.header = new Node(this.MAX_LEVEL,null);
    this.probability = 0.5;
 
   // this.header = new Node [this.MAX_LEVEL + 1];
    //setHeader(this.header);
  };//SkipList()
  
  /**
   * Constructs a new SkipList using default maximum number of levels.
   */
  @SuppressWarnings("unchecked")
  public SkipList(){
    this.MAX_LEVEL = 20;
    this.currentLevel = 1;  
   // this.header = new Node[this.MAX_LEVEL + 1];
    this.header = new Node(this.MAX_LEVEL,null);
    this.probability = 0.5;
    //setHeader(this.header);
  }//SkipList()

  @SuppressWarnings("unchecked")
  // +-------------------------+-----------------------------------------
  // | Internal Helper Methods |
  // +-------------------------+
  
  void setHeader(Node [] head){
    int index = 1;
    
    //set forward pointers of head 
    for(; index<= head.length; index++){
      if(index<= this.currentLevel){
      head[index] = new Node(index, null);
      }//if
      else{
        head[index] = null;
      }//else
    }//for
  }// setHeader()
  
  
  /**
   * Determines a random level for a node
   */
  public  int randomLevel(){
    int newLevel = 1;
    
    //returns a random value between 0 and 1
    Random generator = new Random();
    double random = generator.nextDouble();

    while (random < this.probability){
      newLevel++;
      random = generator.nextDouble(); //spit out another value
    }//while random < probability
    return Math.min(this.MAX_LEVEL, newLevel);
  }

  // +-----------------------+-------------------------------------------
  // | Methods from Iterable |
  // +-----------------------+

  /**
   * Return a read-only iterator (one that does not implement the remove
   * method) that iterates the values of the list from smallest to
   * largest.
   */
  public Iterator<T> iterator()
  {
    // STUB
    return new Iterator<T>(){

      @Override
      public boolean hasNext()
      {
        // TODO Auto-generated method stub
        return false;
      }

      @Override
      public T next()
      {
        // TODO Auto-generated method stub
        return null;
      }
      
    };
  } // iterator()

  // +------------------------+------------------------------------------
  // | Methods from SimpleSet |
  // +------------------------+

  /**
   * Add a value to the set.
   *
   * @post contains(val)
   * @post For all lav != val, if contains(lav) held before the call
   *   to add, contains(lav) continues to hold.
   */
  @SuppressWarnings("unchecked")
  public void add(T val)
  {
   // An array of pointer to nodes that we should update
    Node<T>[] update = new Node[this.MAX_LEVEL+1];
    Node<T> active = this.header;
    int index;
    
    //find and record all the updates
    for(index = this.currentLevel; index >= 1; index--){
      while(active.forwardPointers[index].val.compareTo(val) < 0){
        active = active.forwardPointers[index];
      }//while
      
      //update
      update[index] = active;
    }//for
    
    active = active.forwardPointers[0];
    if(active.val.compareTo(val) == 0){
      return;
    }//if current val equal to val
    else{
      int newLevel = randomLevel();
      if(newLevel > this.currentLevel){
        for(index= this.currentLevel+ 1; index <= newLevel; index++ ){
          update[index] = this.header;
        }//for
        this.currentLevel = newLevel;
      }//if new level greter than current list level
      
      //insert new node
      active = new Node(newLevel,val);
      for(index = 1; index <= newLevel; index++){
        active.forwardPointers[index] = update[index].forwardPointers[index];
        update[index].forwardPointers[index]= active;
      }//for
    }//else
  } // add(T val)

  /**
   * Determine if the set contains a particular value.
   * @param searchVal the value to search by
   */
  public boolean contains(T searchVal)
  {
   Node<T> active = this.header; //point active pointer to the head
   int index;
   
   for(index = this.currentLevel; index >= 1; index--){
     while(active.forwardPointers[index].val.compareTo(searchVal) < 0){
       active = active.forwardPointers[index];
     }//while
   }//for
   active = active.forwardPointers[1];
   return (active.val.compareTo(searchVal) == 0);
  } // contains(T)

  /**
   * Remove an element from the set.
   *
   * @post !contains(val)
   * @post For all lav != val, if contains(lav) held before the call
   *   to remove, contains(lav) continues to hold.
   */
  @SuppressWarnings("unchecked")
  public void remove(T val)
  {
    Node<T> [] update = new Node[this.MAX_LEVEL+1];
    Node<T> active = this.header;
    //find and record update array
    int index;
    for(index = this.currentLevel; index >= 0; index --){
      while(active.forwardPointers[index].val.compareTo(val) < 0){
        active = active.forwardPointers[index];
      }//while
      
      update[index] = active;
    }//for 
    
    active = active.forwardPointers[0];
    
    if(active.val.compareTo(val) ==0){
      for(index=0;index <= this.currentLevel; index++){
        if(update[index].forwardPointers[index] != active ){
          break;
        }//if
        update[index].forwardPointers[index] = active.forwardPointers[index];
      }//for
      
      active = null; //allow to be freed by garbage collector
      
      while(this.currentLevel >1 && 
          this.header.forwardPointers[this.currentLevel] == null){
            this.currentLevel--;
          }//while
    }//if  
  } // remove(T)

  // +--------------------------+----------------------------------------
  // | Methods from SemiIndexed |
  // +--------------------------+

  /**
   * Get the element at index i.
   *
   * @throws IndexOutOfBoundsException
   *   if the index is out of range (index < 0 || index >= length)
   */
  public T get(int i)
  {
    // STUB
    return null;
  } // get(int)

  /**
   * Determine the number of elements in the collection.
   */
  public int length()
  {
    
    return this.size;
  } // length()


} // class SkipList<T>
