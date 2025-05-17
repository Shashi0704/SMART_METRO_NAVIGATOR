#include <iostream>
#include <limits>
#include <string>
#include <cmath>
#include <cstdlib>
#include <vector>
#include <fstream>
#include <sstream>
#include <ctime>
#include <windows.h>
#include <algorithm>
using namespace std;

const int V = 300;

class station_code
{
public:
  string name;
  int code;
  string color;
  vector<string> arrival_time;
};

station_code station[V];
float graph[V][V];

COORD coord;
void gotoxy(int x, int y)
{
  coord.X = x;
  coord.Y = y;
  SetConsoleCursorPosition(GetStdHandle(STD_OUTPUT_HANDLE), coord);
}

string makecapital(string str)
{
  for (char &c : str)
  {
    c = toupper(c);
  }
  return str;
}

int minDistance(float dist[], bool sptSet[])
{
  float min = numeric_limits<float>::max();
  int min_index = -1;
  for (int v = 0; v < V; v++)
  {
    if (!sptSet[v] && dist[v] <= min)
    {
      min = dist[v];
      min_index = v;
    }
  }
  return min_index;
}

void dijkstra(float graph[V][V], int src, int targ)
{
  float dist[V];
  bool sptSet[V];
  int parent[V];

  for (int i = 0; i < V; i++)
  {
    dist[i] = numeric_limits<float>::max();
    sptSet[i] = false;
    parent[i] = -1;
  }
  dist[src] = 0;

  for (int count = 0; count < V - 1; count++)
  {
    int u = minDistance(dist, sptSet);
    if (u == -1)
      break;

    sptSet[u] = true;
    for (int v = 0; v < V; v++)
    {
      if (!sptSet[v] && graph[u][v] > 0 && dist[u] + graph[u][v] < dist[v])
      {
        dist[v] = dist[u] + graph[u][v];
        parent[v] = u;
      }
    }
  }

  vector<int> path;
  int crawl = targ;
  while (crawl != -1)
  {
    path.push_back(crawl);
    crawl = parent[crawl];
  }
  reverse(path.begin(), path.end());

  gotoxy(50, 22);
  cout << "\n\t\t\t\t\t\t......SHORTEST AND FASTEST METRO ROUTE.......\n";
  for (size_t i = 0; i < path.size(); i++)
  {
    cout << station[path[i]].name << " (Arrivals: ";
    for (size_t j = 0; j < station[path[i]].arrival_time.size(); j++)
    {
      cout << station[path[i]].arrival_time[j];
      if (j < station[path[i]].arrival_time.size() - 1)
        cout << ", ";
    }
    cout << ")";
    if (i < path.size() - 1)
      cout << " -----> ";
  }
  cout << endl;
  gotoxy(50, 26);
  cout << "\n\t\t\t\t\t\t\t"
          "TOTAL DISTANCE:: "
       << dist[targ] << " km\n";
  gotoxy(50, 26);

  cout << "\n\t\t\t\t\t\t\t"
          "THANK YOU FOR VISITING\n";
}

int lcs(string X, string Y)
{
  int m = X.length(), n = Y.length();
  vector<vector<int>> L(m + 1, vector<int>(n + 1, 0));

  for (int i = 1; i <= m; i++)
  {
    for (int j = 1; j <= n; j++)
    {
      if (X[i - 1] == Y[j - 1])
        L[i][j] = L[i - 1][j - 1] + 1;
      else
        L[i][j] = max(L[i - 1][j], L[i][j - 1]);
    }
  }
  return L[m][n];
}

int sameMatch(string s)
{
  int max = 0, maxi = -1;
  for (int i = 0; i < V; i++)
  {
    if (station[i].name.empty())
      continue;
    int val = lcs(s, station[i].name);
    if (val > max)
    {
      maxi = station[i].code;
      max = val;
    }
  }
  return (maxi == -1 || max < station[maxi].name.length() / 2) ? -1 : maxi;
}

void take_input()
{
  string start_s, end_s, inter_s;
  gotoxy(50, 5);
  cout << "ENTER THE STARTING STATION:: ";
  getline(cin, start_s);
  gotoxy(50, 7);
  cout << "ENTER THE DESTINATION STATION:: ";
  getline(cin, end_s);
  gotoxy(50, 9);
  cout << "ENTER THE INTERMEDIATE STATION (or type 'NO'):: ";
  getline(cin, inter_s);

  start_s = makecapital(start_s);
  end_s = makecapital(end_s);
  inter_s = makecapital(inter_s);

  int startcode = sameMatch(start_s);
  int endcode = sameMatch(end_s);
  int intercode = sameMatch(inter_s);
  bool interflag1 = (inter_s != "NO" && inter_s != "N");

  if (startcode == -1 || endcode == -1 || (interflag1 && intercode == -1))
  {
    gotoxy(50, 12);
    cout << "INVALID STATION NAME ENTERED. TRY AGAIN.\n";
    return;
  }

  gotoxy(50, 16);
  cout << "\n\t\t\t\t\t\t       ...CALCULATING SHORTEST PATH...\n";
  gotoxy(50, 18);
  cout << "\n\t\t\t\t\t\t       ...ARRIVAL TIME OF 3 METRO...\n";

  if (interflag1)
  {
    dijkstra(graph, startcode, intercode);
    dijkstra(graph, intercode, endcode);
  }
  else
  {
    dijkstra(graph, startcode, endcode);
  }
}

void UI()
{
  system("color 0F");
  gotoxy(40, 2);
  cout << "=======================================================";
  gotoxy(45, 3);
  cout << "         "
          "WELCOME TO METRO DESKTOP APP";
  gotoxy(40, 4);
  cout << "=======================================================";

  gotoxy(45, 6);
  cout << "         "
          "PRESS ENTER TO CONTINUE...";
  cin.ignore();
  system("cls");
  take_input();
}

int main()
{
  int temp, n1, n2;
  float dis;

  // Read graph data
  ifstream nodeValuesFile("node_values_new.txt");
  if (!nodeValuesFile)
  {
    cerr << "Error opening node_values_new.txt" << endl;
    return 1;
  }

  // Initialize graph
  for (int i = 0; i < V; i++)
    for (int j = 0; j < V; j++)
      graph[i][j] = 0;

  while (nodeValuesFile >> temp >> n1)
  {
    for (int j = 0; j < temp; j++)
    {
      nodeValuesFile >> n2 >> dis;
      if (n1 > 0 && n2 > 0 && n1 <= V && n2 <= V)
      {
        graph[n1 - 1][n2 - 1] = dis;
        graph[n2 - 1][n1 - 1] = dis; // Undirected
      }
    }
  }

  // Read stations and color codes
  ifstream stationFile("station.txt");
  if (!stationFile)
  {
    cerr << "Error opening station.txt" << endl;
    return 1;
  }

  ifstream colorCodesFile("colorcodes.txt");
  if (!colorCodesFile)
  {
    cerr << "Error opening colorcodes.txt" << endl;
    return 1;
  }

  string stationLine, colorLine;
  int index = 0;
  while (getline(stationFile, stationLine) && getline(colorCodesFile, colorLine) && index < V)
  {
    station[index].name = stationLine;
    station[index].code = index;
    station[index].color = colorLine;
    ++index;
  }

  // Read arrival times
  ifstream arrivalTimeFile("arrival_times.txt");
  if (!arrivalTimeFile)
  {
    cerr << "Error opening arrival_times.txt" << endl;
    return 1;
  }

  string line;
  index = 0;
  while (getline(arrivalTimeFile, line) && index < V)
  {
    stringstream ss(line);
    string time;
    while (getline(ss, time, ','))
    {
      station[index].arrival_time.push_back(time);
    }
    ++index;
  }

  // Start UI
  UI();
  return 0;
}
