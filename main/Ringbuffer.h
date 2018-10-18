/*
 *	RingBuffer.h
 *
 *	copyright (c) Andreas Kraft 2018
 *	Licensed under the BSD 3-Clause License. See the LICENSE file for further details.
 *
 *	Implementation of a non-blocking ring-buffer class template.
 */

# ifndef __RINGBUFFER_H__
# define __RINGBUFFER_H__

template <typename T> 
class RingBuffer {

private:
	T 	*_buffer;
	int	 _size;
	int  _index;
	int	 _count;

	// calculate the absolute position of an item in the buffer
	int 			 _relativeToFirst(int pos);		

public:
	RingBuffer(int size);
	~RingBuffer();

	//	Add an *item* to the end of the ring buffer. If the buffer
	//	is full, then the oldest item in the buffer is overwritten.
	void 			 add(T item);

	// Get an item from the ring buffer. *index* is relative to the
	// beginning of the buffer, ie ```get(0)``` returns the oldest item
	// while ```get(count())``` returns the newest item from the buffer.
	// If *index* is invalid, a new object of Type T is returned.

	T 				 get(int index);

	// Get an item from the ring buffer. This method acts exactly in reverse
	// to the *get()* method, ie ```get(0)``` returns the newest item
	// while ```get(count())``` returns the oldest item from the buffer.
	// If *index* is invalid, a new object of Type T is returned.

	T 				 getReverse(int index);

	// Get the oldest item from the ring buffer.
	// If the buffer is empty, a new object of Type T is returned.
	T 				 getOldest();

	// Get the latest item from the ring buffer.
	// If the buffer is empty, a new object of Type T is returned.
	T 				 getLatest();

	// Remove *count* elements from the tail and head of the ring buffer.
	// The size of the ring buffer is reduced by *count* * 2 items.
	// The method returns true if successful, false otherwiese.
	bool 			 slice(int count);

	// Remove *count* elements from the head of the ring buffer.
	// The size of the ring buffer is reduced by *count* items.
	// The method returns true if successful, false otherwiese.
	bool			 sliceHead(int count);

	// Remove *count* elements from the tail of the ring buffer.
	// The size of the ring buffer is reduced by *count* items.
	// The method returns true if successful, false otherwiese.
	bool			 sliceTail(int count);

	// Empty the ring buffer.
	void 			 clear();

	// Return the size of the ring buffer.
	int 			 size();

	// Return the number of elements in the ring buffer.
	int 			 count();

	// Check whether the ring buffer is empty.
	bool 			 isEmpty();

	// Check whether the ring buffer is full.
	bool	 		 isFull();

	// Using this assignment operator is equivalent to calling the *get()* method.
	T  				 operator[](int index);

	// Using this assignment operator is equivalent to calling the *add()* method.
	RingBuffer<T>	&operator=(T item);
};

#endif