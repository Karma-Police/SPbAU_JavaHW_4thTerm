#include <iostream>
#include <cstdio>
#include <cstdlib>
#include <string>

using namespace std;

typedef long long ll;
string s;

ll a[3], b[3], c[3];

int main()
{
    cin >> s;
    for (size_t i = 0; i < s.length(); i++)
    {
        if (s[i] == 'B') ++a[0];
        if (s[i] == 'S') ++a[1];
        if (s[i] == 'C') ++a[2];
    }

    for (int i = 0; i < 3; i++)
        cin >> b[i];

    for (int i = 0; i < 3; i++)
        cin >> c[i];

    ll m; cin >> m;
    ll ans = 0;


    for (int j = 0; j < 101; j++)
    {
        for (int i = 0; i < 3; i++)
        {
            if (b[i] < a[i]) {
                ll tmp = (a[i] - b[i]) * c[i];
                if (tmp > m) {
                    cout << ans << endl;
                    return 0;
                }
                m -= tmp;
                b[i] = 0;
            } else {
                b[i] -= a[i];
            }
        }
        ++ans;
    }

    ll tmp = a[0] * c[0] + a[1] * c[1] + a[2] * c[2];
    ans += m / tmp;
    cout << ans << endl;
    return 0;
}

