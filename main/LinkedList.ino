
/*
 *	LinkedList.ino
 *
 *	copyright (c) Andreas Kraft 2018
 *	Licensed under the BSD 3-Clause License. See the LICENSE file for further details.
 *
 *	Implementation of a single-linked list class template.
 */

# include "LinkedList.h"

// Constructor
template<typename T>
LinkedList<T>::LinkedList() {
	head = NULL;
	cnt = 0;
}

// Destructor
template<typename T>
LinkedList<T>::~LinkedList() {
	clear();
}

template<typename T>
int LinkedList<T>::size() {
	return cnt;
}


template<typename T>
bool LinkedList<T>::add(T object, int position) {
	if (position < 0 || position > cnt) {
		return false;
	}

	LNode<T> *tmp = new LNode<T>();
	tmp->data = object;

	if (position == 0) {
		tmp->next = head;
		head = tmp;
	} else {
		LNode<T> *node = _getNodeAtPosition(position-1);
		tmp->next = node->next;
		node->next = tmp;
	}
	cnt++;
	return true;
}


template<typename T>
bool LinkedList<T>::add(T object) {
	return add(object, cnt);
}


template<typename T>
T LinkedList<T>::get(int position) {
	LNode<T> *node = _getNodeAtPosition(position);
	if (node)
		return node->data;
	return T();
}


template<typename T>
T LinkedList<T>::first() {
	return get(0);
}


template<typename T>
T LinkedList<T>::last() {
	return get(cnt-1);
}


template<typename T>
bool LinkedList<T>::remove() {
	return remove(cnt-1);
}


template<typename T>
bool LinkedList<T>::remove(int position) {
	if (position < 0 || position >= cnt) {
		return false;
	}

	LNode<T> *node = _getNodeAtPosition(position);

	if (position == 0) {		// first
		head = node->next;
	} else {
		LNode<T> *prev = _getNodeAtPosition(position-1);
		prev->next = node->next;
	}

	delete(node);
	cnt--;
	return true;
}


template<typename T>
void LinkedList<T>::clear() {
	while (cnt > 0) {
		remove(0);
	}
}


template<typename T>
LNode<T> * LinkedList<T>::_getNodeAtPosition(int position) {
	 if (position < 0 || position > cnt) {
		return NULL;
	}

	LNode<T> *node = head;
	for (int i = 0; i < position; i++) {
		node = node->next;
	}
	return node;
}
