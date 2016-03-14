#include <iostream>
#include <cstdio>
#include <cassert>
#include <type_traits>

using namespace std;

struct tester {
  class it {};
  typedef it MyIterator;
};

template<class T>
class has_iterator
{
public:
  template<typename U>
  static char test(typename U::MyIterator);

  template<typename U>
  static int test(...);

  static const bool value = (sizeof(test<T>(0)) == 1);
};

template<class T>
typename enable_if<has_iterator<T>::value, void>::type show(T)
{
  cout << "has iterator" << endl;
}

template<class T>
typename enable_if<!has_iterator<T>::value, void>::type show(T)
{
  cout << "no iterator" << endl;
}

int main()
{
  show(has_iterator<tester>());
}

