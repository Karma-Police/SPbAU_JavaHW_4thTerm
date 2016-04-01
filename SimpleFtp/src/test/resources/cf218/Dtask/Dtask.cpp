#include <iostream>
#include <cstdio>
#include <cstdlib>
#include <cassert>

using namespace std;

typedef long long ll;

int N = 0;

int tr[2000000];
int water[300000], inp[300000];

int get_rt(int l, int r, int L, int R, int x)
{
    if (l > R || r < L) return 1000000;
    if (l <= L && r >= R) return tr[x];

    int m = (L + R) / 2;
    return min(get_rt(l, r, L, m, x * 2),
               get_rt(l, r, m + 1, R, x * 2 + 1));
}

void upd(int pos)
{
    tr[pos] = 1000000;
    pos /= 2;
    while (pos) {
        tr[pos] = min(tr[pos * 2], tr[pos * 2 + 1]);
        pos /= 2;
    }
}

int main()
{
//    freopen("in.in", "r", stdin);

    int n; cin >> n;

    int l = 0;
    while ((1 << l) < n) ++l;
    N = (1 << l);

    for (int i = N ; i < N * 2; i++)
        if (i - N < n) tr[i] = i - N + 1;
        else tr[i] = 1000000;

    for (int i = 1; i <= n; i++)
    {
        assert(scanf("%d", &water[i]) == 1);
        inp[i] = water[i];
    }

    for (int i = N - 1; i > 0; i--)
        tr[i] = min(tr[i * 2], tr[i * 2 + 1]);

    int m; cin >> m;
    for (int i = 0; i < m; i++)
    {
        int tp;
        assert(scanf("%d", &tp) == 1);
        if (tp == 1) {
            int nm, wt;
            assert(scanf("%d%d", &nm, &wt));
//            nm = n - nm + 1;
            while (wt) {
                int cur = get_rt(N + nm - 1, N * 2 - 1, N, N * 2 - 1, 1);
//                cout << cur << " ";
                if (cur == 1000000) wt = 0;
                else {
                    if (water[cur] >= wt) {
                        water[cur] -= wt;
                        wt = 0;
                        if (!water[cur]) upd(cur + N - 1);
                    } else {
                        wt -= water[cur];
                        water[cur] = 0;
                        upd(cur + N - 1);
                    }
                }
            }
//            cout << endl;
        } else {
            int nm; assert(scanf("%d", &nm) == 1);
            cout << inp[nm] - water[nm] << endl;
        }
    }
    return 0;
}


