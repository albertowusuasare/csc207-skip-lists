package taojava.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Random;

import static org.junit.Assert.*;

import org.junit.Test;

import taojava.util.SortedList;

/**
 * Generic tests of sorted lists.
 *
 * To test a particular implementation of sorted lists, subclass this
 * class and add an appropriate @Before clause to fill in strings and
 * ints.
 * 
 * @author Samuel A. Rebelsky
 * @author Albert Owusu-Asare
 */
public class SortedListTest
{
  // +--------+----------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * A sorted list of strings for tests.  (Gets set by the subclasses.)
   */
  SortedList<String> strings;

  /**
   * A sorted list of integers for tests.  (Gets set by the subclasses.)
   */
  SortedList<Integer> ints;

  /**
   * A random number generator for the randomized tests.
   */
  Random random = new Random();

  /**
   * A switch that tells if implementation allowed copies of values of not
   */

  boolean duplicationAllowed = false;

  // +---------+---------------------------------------------------------
  // | Helpers |
  // +---------+

  /**
   * Dump a SortedList to stderr.
   */
  static <T extends Comparable<T>> void dump(SortedList<T> slist)
  {
    System.err.print("[");
    for (T val : slist)
      {
        System.err.print(val + " ");
      } // for
    System.err.println("]");
  } // dump

  /**
   * Determine if an iterator only returns values in non-decreasing
   * order.
   */
  static <T extends Comparable<T>> boolean inOrder(Iterator<T> it)
  {
    // Simple case: The empty iterator is in order.
    if (!it.hasNext())
      return true;
    // Otherwise, we need to compare neighboring elements, so
    // grab the first element.
    T current = it.next();
    // Step through the remaining elements
    while (it.hasNext())
      {
        // Get the next element
        T next = it.next();
        // Verify that the current node <= next
        if (current.compareTo(next) > 0)
          {
            return false;
          } // if (current > next)
        // Update the current node
        current = next;
      } // while
    // If we've made it this far, everything is in order
    return true;
  } // inOrder(Iterator<T> it)

  /**
   * Tells if an element of an array is null
   * @param arr the array 
   * @return <code> True </code> if an element is null<br>
   *         <code> False </code> if no null element found
   */
  static <T> boolean containsNull(T[] arr)
  {
    boolean found = false;
    for (T val : arr)
      {
        if (val == null)
          {
            found = true;
            break;
          }//if
      }//for
    return found;
  }

  static <T extends Comparable<T>> String[]
    addDummyData(SortedList<String> slist)
  {

    String[] dummyData =
        { "Ronaldo", "Messi", "Ronaldinho", "Paulinho", "Rooney" + "Beckham",
         "Zidane", "Figo", "Vierra", "Rivaldo" };

    for (String str : dummyData)
      {
        slist.add(str);
      }//for
    return dummyData;
  }

  // +-------------+-----------------------------------------------------
  // | Basic Tests |
  // +-------------+

  /**
   * A really simple test.  Add an element and make sure that it's there.
   */
  @Test
  public void simpleTest()
  {
    strings.add("hello");
    assertTrue(strings.contains("hello"));
    assertFalse(strings.contains("goodbye"));
  } // simpleTest()

  /**
   * Another simple test.  The list should not contain anything when
   * we start out.
   */
  @Test
  public void emptyTest()
  {
    assertFalse(strings.contains("hello"));
  } // emptyTest()

  /**
   * A simple test for the iterator
   */
  @Test
  public void IteratorBasicTest()
  {

    //populate lst with integers
    for (int i = 0; i < 10; i++)
      {
        ints.add(i);
      }//for
    Iterator<Integer> iterator = ints.iterator();
    assertTrue(iterator.next() == 0);
    //move to the middle of the list
    int counter = 1;
    while (iterator.hasNext() && counter <= 5)
      {
        iterator.next();
        counter++;
      }//while
    assertTrue(iterator.next() == counter);
    //delete current element
    iterator.remove();
    assertFalse(ints.contains(counter));
  }//iteratorTest()

  /**
   * A simple test for the fail fast.
   * This test make sure that the semantics underlying an iterator or followed.
   */

  @Test(expected = ConcurrentModificationException.class)
  public void iteratorSemanticsTest()
  {
    //add an element to the list
    ints.add(1);
    ints.add(2);
    ints.add(3);

    // remove an element using an iterator
    Iterator<Integer> iterator = ints.iterator();
    Iterator<Integer> evilIter = ints.iterator();
    iterator.next();
    iterator.remove();
    evilIter.remove();
  }//iteratorSemanticsTest()

  /**
   * A simple test for when asked to remove null object
   */
  @Test
  public void removeNullTest()
  {

    String[] data = addDummyData(strings);
    // remove null valued object from the list and observe if there is a change 
    strings.remove(null);
    String[] temp = new String[data.length];

    Iterator<String> iterator = strings.iterator();
    int i = 0;
    while (iterator.hasNext())
      {
        temp[i] = iterator.next();
        i++;
      }//while
    assertTrue(!containsNull(temp));
  }//removeTest()

  @Test
  /**
   * Simple test to when all elements are removed from list
   */
  public void removeAllTest()
  {

    String[] data = addDummyData(strings);
    for (int i = 0; i < data.length; i++)
      {
        strings.remove(data[i]);
      }//for

    //check if list is null
    Iterator<String> iterator = strings.iterator();
    boolean hasNext = iterator.hasNext();
    assertFalse(hasNext);
  }//removeAllTest()

  /** 
   * Test repeated values on List
   */
  @Test
  public void repeatedValuesTest()
  {
    int repeat = 2;
    boolean duplicateFound = false;
    //loop through adding a value to the list repeat number of times
    for (int i = 0; i < 10; i++)
      {
        while (repeat > 0)
          {
            ints.add(i);
            repeat--;
          }//while
      }//for
    //check if only one copy found

    for (int i = 0; i < 10; i++)
      {
        ints.remove(i);
        duplicateFound = ints.contains(i);

        if (duplicateFound && !duplicationAllowed)
          fail("duplicate element " + i + "found");
        else
          {
            if (!duplicateFound && duplicationAllowed)
              fail("duplicates for " + i + " not found");
          }//else 
      }//for
  }//testRepeatedValues()

  /**
   * A simple test for when a null object is added
   */

  @Test
  public void addNullTest()
  {
    String[] data = addDummyData(strings);
    strings.add(null);
    //store all the current elements on the list
    ArrayList<String> lst = new ArrayList<String>();
    for (String val : strings)
      {
        lst.add(val);
      }//for
    assertTrue(data.length == lst.size());
  }// 

  // +-----------------+-------------------------------------------------
  // | RandomizedTests |
  // +-----------------+

  /**
   * Verify that a randomly created list is sorted.
   */
  @Test
  public void testOrdered()
  {
    // For reporting errors: an array of operations
    ArrayList<String> operations = new ArrayList<String>();
    // Add a bunch of values
    for (int i = 0; i < 100; i++)
      {
        int rand = random.nextInt(1000);
        ints.add(rand);
        operations.add("ints.add(" + rand + ")");
      } // for
    if (!inOrder(ints.iterator()))
      {
        System.err.println("inOrder() failed");
        for (String op : operations)
          System.err.println(op + ";");
        dump(ints);
        fail("The instructions did not produce a sorted list.");
      } // if the elements are not in order.
  } // testOrdered()

  /**
   * Verify that a randomly created list contains all the values
   * we added to the list.
   */
  @Test
  public void testContainsOnlyAdd()
  {
    ArrayList<String> operations = new ArrayList<String>();
    ArrayList<Integer> vals = new ArrayList<Integer>();
    // Add a bunch of values
    for (int i = 0; i < 100; i++)
      {
        int rand = random.nextInt(200);
        vals.add(rand);
        operations.add("ints.add(" + rand + ")");
        ints.add(rand);
      } // for i
    // Make sure that they are all there.
    for (Integer val : vals)
      {
        if (!ints.contains(val))
          {
            System.err.println("contains(" + val + ") failed");
            for (String op : operations)
              System.err.println(op + ";");
            dump(ints);
            fail(val + " is not in the sortedlist");
          } // if (!ints.contains(val))
      } // for val
  } // testContainsOnlyAdd()

  /**
   * An extensive randomized test.
   */
  @Test
  public void randomTest()
  {
    // Set up a list of all the operations we performed.  (That way,
    // we can replay a failed test.)
    ArrayList<String> operations = new ArrayList<String>();
    // Keep track of the values that are currently in the sorted list.
    ArrayList<Integer> vals = new ArrayList<Integer>();

    // Add a bunch of values
    for (int i = 0; i < 1000; i++)
      {
        boolean ok = true;
        int rand = random.nextInt(2000);
        // Half the time we add
        if (random.nextBoolean())
          {
            if (!ints.contains(rand))
              vals.add(rand);
            operations.add("ints.add(" + rand + ")");
            ints.add(rand);
            if (!ints.contains(rand))
              {
                System.err.println("After adding " + rand + " contains fails");
                ok = false;
              } // if (!ints.contains(rand))
          } // if we add
        // Half the time we remove
        else
          {
            operations.add("ints.remove(" + rand + ")");
            ints.remove(rand);
            vals.remove((Integer) rand);
            if (ints.contains(rand))
              {
                System.err.println("After removing " + rand
                                   + " contains succeeds");
                ok = false;
              } // if ints.contains(rand)
          } // if we remove
        // See if all of the appropriate elements are still there
        for (Integer val : vals)
          {
            if (!ints.contains(val))
              {
                System.err.println("ints no longer contains " + val);
                ok = false;
                break;
              } // if the value is no longer contained
          } // for each value
        // Dump the instructions if we've encountered an error
        if (!ok)
          {
            for (String op : operations)
              System.err.println(op + ";");
            dump(ints);
            fail("Operations failed");
          } // if (!ok)
      } // for i
  } // randomTest()
} // class SortedListTest
