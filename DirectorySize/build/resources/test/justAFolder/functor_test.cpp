#include <iostream>
#include <cstdio>
#include <cassert>
#include <list>
#include <algorithm>

using namespace std;

class checker {
public:
  int var;
  checker(int _var = 2) : var(_var) { };
  bool operator () (int a) {
    return (a % var == 0);
  }
};

struct max_checker {
  bool operator () (int a, int b) {
    if (a == b) return 0;
    if (b == 6) return 1;
    return 0;
  }
};

int main()
{
  list<int> l = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
  list<int> l1 = {1, 2, 3, 4, 5, 6, 7, 8, 9};
  cout << count(l.begin(), l.end(), 9) << endl;
  cout << count_if(l.begin(), l.end(), checker(3)) << endl;
  cout << equal(l1.begin(), l1.end(), l.begin()) << endl;
  cout << *max_element(l.begin(), l.end(), max_checker()) << endl;
}
