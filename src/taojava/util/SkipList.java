package taojava.util;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * A randomized implementation of sorted lists.  
 * 
 * @author Samuel A. Rebelsky
 * @author Albert Owusu-Asare
 * 
 */

/*
 * Citations:
 * Implementations done here were highly motivated by
 * discussions with Yazan Kittaneh <kittaneh17 @grinnell.edu>
 * Zoe Wolter <wolterzo@grinnell.edu> and Ameer Shujjah  <shujjah@grinnell.edu>
 * 
 * Compared get(i) implementation with that of Patrick Slough's
 *  <sloughPa@grinnell.edu>
 * Online resources consulted include:
 *  http://en.literateprograms.org/Skip_list_(Java)
 */
public class SkipList<T extends Comparable<T>>
    implements SortedList<T>
{
  // +--------+----------------------------------------------------------
  // | Fields |
  // +--------+

  //the maximum level of the list
  int MAX_LEVEL;
  //the  current level number of the list
  int currentLevel;
  //size of the list
  int size;
  //Probability used in calculating random level
  double probability;
  //tracks modifications to the list
  int mods = 0;

  //the front of the list stores references to all the 
  //different levels from 0 to MaxLevel
  Node<T> head;
  // the tail end of the list
  Node<T> tail;

  // +------------------+------------------------------------------------
  // | Internal Classes |
  // +------------------+

  /**
   * Nodes for skip lists.
   */
  @SuppressWarnings("hiding")
  public class Node<T>
  {
    // +--------+--------------------------------------------------------
    // | Fields |
    // +--------+

    /**
     * The value stored in the node.
     */
    T val;

    /**
     *  Array of forward pointers
     */
    Node<T>[] forwardPointers;

    /**
     * Constructs a new node
     * @param nodeLevel the level this node is at.
     * @param data the data to be stored in the node.
     */
    @SuppressWarnings("unchecked")
    public Node(int nodeLevel, T data)
    {
      this.val = data;
      // we will use index 1 to nodeLevel for our forward pointers
      this.forwardPointers = new Node[nodeLevel + 1];
      //initialize all the pointers to null upon creation
      for (int i = 0; i < nodeLevel; i++)
        {
          this.forwardPointers[i] = null;
        }//for   
    }//Node(int, T) 
  } // class Node

  // +--------------+----------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Constructs a new SkipList using default maximum number of levels.
   */
  public SkipList()
  {
    this(20);
  }//SkipList()

  /**
   * Constructs a new SkipList using a maximum number of levels.
   * @param maxLevel the maximum level for the list
   */
  public SkipList(int maxLevel)
  {
    this(maxLevel, 0.5);
  }//SkipList(int)

  /**
   * Constructs a new SkipList using  a given probability and maximum length.
   * @param maxLevel the maximum number of levels in the skip List
   * @param probability the probability used in assinging random levels
   */
  @SuppressWarnings("unchecked")
  public SkipList(int maxLevel, double probability)
  {
    this.currentLevel = 0;
    this.probability = probability;
    this.MAX_LEVEL = maxLevel;
    this.head = new Node<T>(this.MAX_LEVEL, null);
    this.tail = new Node<T>(this.MAX_LEVEL, null);
    for (int i = 0; i < this.MAX_LEVEL; i++)
      {
        this.tail.forwardPointers[i] = null;
        this.head.forwardPointers[i] = tail;
      }//for
    // setHeadToTail(this.head, this.tail);
  };//SkipList(int, double)

  void setHeadToTail(Node<T> head, Node<T> tail)
  {
    //set tail forward pointers to null and set
    // head forward pointers to tail Node
    for (int i = 0; i < this.MAX_LEVEL; i++)
      {
        tail.forwardPointers[i] = null;
        head.forwardPointers[i] = tail;
      }//for
  }// setHeadToTail(Node, Node)

  /**
   * Determines a random level for a node
   */
  public int randomLevel()
  {
    int newLevel = 0;
    Random generator = new Random();

    while (generator.nextDouble() < this.probability)
      {
        newLevel++;
      }//while random < probability
    return Math.min(this.MAX_LEVEL - 1, newLevel);
  }// randomLevel

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

    return new Iterator<T>()
      {

        //Fields 
        Node<T> cursor = SkipList.this.head;
        int mods = SkipList.this.mods;

        //Helper method
        /**
         * Checks if the list has been updated since <code>this</code> Iterator
         * was modified
         */

        void failFast()
        {
          if (this.mods != SkipList.this.mods)
            throw new ConcurrentModificationException();
        }//failFast()

        @Override
        public boolean hasNext()
        {
          failFast();
          return this.cursor.forwardPointers[0].val != null;
        }//hasNext()

        @Override
        public T next()
        {
          failFast();
          //check if there is actually a next value
          if (!this.hasNext())
            {
              throw new NoSuchElementException();
            }//if no next value
          this.cursor = this.cursor.forwardPointers[0];
          return this.cursor.val;

        }//next()

        @Override
        public void remove()
        {
          // move to the position right before the current cur
          failFast();
          SkipList.this.remove(this.cursor.val);
          this.mods++;
          SkipList.this.size--;
        }//remove() 
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

  public void add(T val)
  {
    // An array of pointer to nodes that we should update
    @SuppressWarnings("unchecked")
    Node<T>[] update = new Node[this.MAX_LEVEL];
    Node<T> active = this.head;

    //assert if val is null
    if (val == null)
      {
        return;
      }//if
    int i;

    //find and record all the updates
    for (i = this.currentLevel; i >= 0; i--)
      {
        while ((active.forwardPointers[i].val != null)
               && active.forwardPointers[i].val.compareTo(val) < 0)
          {
            active = active.forwardPointers[i];
          }//while
           //update
        update[i] = active;
      }//for

    //reset active
    active = active.forwardPointers[0];

    if (active.val != null && active.val.compareTo(val) == 0)
      {
        return;
      }//if current val equal to val

    else
      {
        int newLevel = randomLevel();
        if (newLevel > this.currentLevel)
          {
            for (i = this.currentLevel + 1; i <= newLevel; i++)
              {
                update[i] = this.head;
              }//for
            this.currentLevel = newLevel;
          }//if new level greater than current list level

        //insert new node
        active = new Node<T>(newLevel, val);
        for (i = 0; i <= newLevel; i++)
          {
            active.forwardPointers[i] = update[i].forwardPointers[i];
            update[i].forwardPointers[i] = active;
          }//for
      }//else
    this.mods++;
    this.size++;
  } // add(T val)

  /**
   * Determine if the set contains a particular value.
   * @param searchVal the value to search by
   */
  public boolean contains(T searchVal)
  {
    Node<T> active = this.head; //point active pointer to the head
    int i;

    for (i = this.currentLevel; i >= 0; i--)
      {
        while ((active.forwardPointers[i].val != null)
               && active.forwardPointers[i].val.compareTo(searchVal) < 0)
          {
            active = active.forwardPointers[i];
          }//while
      }//for

    //check to see if there is a next 
    if (active.forwardPointers[0].val == null)
      {
        return false;
      }//if
    active = active.forwardPointers[0];
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
    Node<T>[] update = new Node[this.MAX_LEVEL];
    Node<T> active = this.head;

    if (val == null)
      {
        return;
      }// if the argument passed in is a null object

    //find and record update array
    int i;//loop variable
    for (i = this.currentLevel; i >= 0; i--)
      {
        while ((active.forwardPointers[i].val != null)
               && active.forwardPointers[i].val.compareTo(val) < 0)
          {
            active = active.forwardPointers[i];
          }//while
        update[i] = active;
      }//for 

    active = active.forwardPointers[0];

    if (active.val != null && active.val.compareTo(val) == 0)
      {
        for (i = 0; i <= this.currentLevel; i++)
          {
            if (update[i].forwardPointers[i] != active)
              break;
            update[i].forwardPointers[i] = active.forwardPointers[i];
          }//for
        active = null; //allow to be freed by garbage collector

        while (this.currentLevel > 0
               && this.head.forwardPointers[this.currentLevel] == null)
          {
            this.currentLevel--;
          }//while
        this.mods++;
        this.size--;
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
    if ((i < 0) || (i > this.size))
      {
        throw new IndexOutOfBoundsException();
      }//check bounds

    Node<T> current = this.head.forwardPointers[0];
    int counter = 0;
    while (counter < i)
      {
        current = current.forwardPointers[0];
        counter++;
      }//while
    return current.val;
  } // get(int)

  /**
   * Determine the number of elements in the collection.
   */
  public int length()
  {
    return this.size;
  } // length()

} // class SkipList<T>
