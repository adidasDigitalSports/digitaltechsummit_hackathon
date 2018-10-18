/*
 *	RingBuffer.ino
 *
 *	copyright (c) Andreas Kraft 2018
 *	Licensed under the BSD 3-Clause License. See the LICENSE file for further details.
 *
 *	Implementation of a non-blocking ring-buffer class template.
*/

#include "Ringbuffer.h"

template<typename T>
RingBuffer<T>::RingBuffer(int size) {
	_buffer = new T[size];
	_size = size;
	clear();
}


template<typename T>
RingBuffer<T>::~RingBuffer() {
	delete _buffer;
}


template<typename T>
void RingBuffer<T>::add(T item) {
	_buffer[_index++] = item;
	_index %= _size;
	_count = _count == _size ? _count : _count +1;
}


template<typename T>
T RingBuffer<T>::get(int index) {	// relative get, starting with the first in the buffer
	if (index < 0 || index >= _size) {
		return T();
	}
	return _buffer[_relativeToFirst(index)];
}


template<typename T>
T RingBuffer<T>::getReverse(int index) {	// relative get, starting with the last in the buffer
	if (index < 0 || index >= _size) {
		return T();
	}
	return _buffer[_relativeToFirst(_count - index - 1)];
}


template<typename T>
T RingBuffer<T>::getOldest() {	// get relative first elemnt
	return get(0);
}


template<typename T>
T RingBuffer<T>::getLatest() {	// get relative last elemnt
	return getReverse(0);
}


template<typename T>
void RingBuffer<T>::clear() {
	_index = 0;
	_count = 0;
}


template<typename T>
int RingBuffer<T>::size() {
	return _size;
}


template<typename T>
int RingBuffer<T>::count() {
	return _count;
}


template<typename T>
bool RingBuffer<T>::sliceHead(int count) {
	if (count > _count) {
		return false;
	}
	_count -= count;
	_index = (_index - count + _size) % _size;
	return true;
}


template<typename T>
bool RingBuffer<T>::sliceTail(int count) {
	if (count > _count) {
		return false;
	}
	_count -= count;
	return true;
}


template<typename T>
bool RingBuffer<T>::slice(int count) {
	if (count * 2 > _count) {
		return false;
	}
	return sliceHead(count) && sliceTail(count);
}


template<typename T>
bool RingBuffer<T>::isEmpty() {
	return _count == 0;
}


template<typename T>
bool RingBuffer<T>::isFull() {
	return _count == _size;
}


template<typename T>
T RingBuffer<T>::operator[](int index) {
	return get(index);
}


template<typename T>
RingBuffer<T> &RingBuffer<T>::operator=(T item) {
	add(item);
	return *this;
}


template<typename T>
int RingBuffer<T>::_relativeToFirst(int pos) {
	return (_size - _count + _index + pos) % _size;
}
/*

template<typename T>
int RingBuffer<T>::_removeDuplicates() {

}

void rmDup(int array[], int& size) {
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                if (array[i] == array[j]) {
                    array[i - 1 ] = array[i];
                    size--;
                }
            }
        }
}
