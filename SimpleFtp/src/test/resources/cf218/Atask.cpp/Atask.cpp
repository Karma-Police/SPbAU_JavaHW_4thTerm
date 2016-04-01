#include <iostream>
#include <cstdio>
#include <string>
#include <vector>
#include <map>

using namespace std;

const int INF = int(1e8);

string s = "";

int n_ok[101];

int main()
{
    int n, k;
    cin >> n >> k;

    for (int i = 0; i < n; i++)
    {
        char tmp; cin >> tmp;
        s += tmp;
    }

    int ans = 0;

    for (int i = k; i <= n - k; i += k)
    {
        for (int j = 0; j < k; j++)
            if (s[j] != s[i + j]) {
                ++n_ok[j];
            }
    }

    for (int i = 0; i < k; i++)
    {
        ans += min(n_ok[i], n / k - n_ok[i]);
    }
    cout << ans << endl;
    return 0;
}

