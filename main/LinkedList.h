/*
 *	LinkedList.h
 *
 *	copyright (c) Andreas Kraft 2018
 *	Licensed under the BSD 3-Clause License. See the LICENSE file for further details.

 *
 *	Implementation of a single-linked list class template.
 */

# ifndef __LINKEDLIST_H__
# define __LINKEDLIST_H__


// Representation of a single node in the list.
template<class T>
struct LNode {
	T 			 data;
	LNode<T> 	*next;
};


// Linked List template class
template <typename T>
class LinkedList {

protected:
	int 		 cnt;
	LNode<T>	*head;

	LNode<T> 	*_getNodeAtPosition(int position);

public:
	LinkedList();
	~LinkedList();

	// Return the number of items in the linked list.
	int 	size();

	// Append an *object* to the end of the list.
	// This method returns false if the *object* couldn't be appended, true otherwise.
	bool	add(T);

	// Add an *object* at *position* in the list.
	// This method returns false if *position* points to an invalid index, true otherwise.
	bool 	add(T, int position);

	// Return the object at position *position*.
	// This method returns a new object of class *T* in case of an error.
	T 		get(int position);

	// Return the first object from the list.
	// This method returns a new object of class *T* in case of an error.
	T 		first();

	// Return the first object from the list.
	// This method returns a new object of class *T* in case of an error.
	T 		last();

	// Remove the last object from the list. The object is deleted.
	// This method returns true if successful, or false otherwise.
	bool	remove();

	// Remove the object at position *position*. The object is deleted.
	// This method returns true if successful, or false otherwise.
	bool	remove(int position);

	// Clear the list of all objects. The objects are deleted.
	void	clear();

};


# endif
