package dev.algolens.analysis;

import dev.algolens.user.Language;
import java.util.*;

public final class PatternCatalog {
 private PatternCatalog(){}
 public record Pattern(String id,String name,String complexity,String reason,String clue,Map<Language,String> code){}
 private static Map<Language,String> code(String java,String cpp,String py){return Map.of(Language.JAVA,java,Language.CPP,cpp,Language.PYTHON,py);}
 public static final List<Pattern> ALL=List.of(
  new Pattern("prefix-suffix-product","Prefix & Suffix Product Scan","O(n) time · O(1) space","Scans products from both ends so negative values are handled without storing a DP table.","Best for Maximum Product Subarray when zeros reset the running product.",code(
   "int prefix=1,suffix=1,best=nums[0];\nfor(int i=0;i<nums.length;i++){\n if(prefix==0)prefix=1; if(suffix==0)suffix=1;\n prefix*=nums[i]; suffix*=nums[nums.length-1-i];\n best=Math.max(best,Math.max(prefix,suffix));\n}",
   "int prefix=1,suffix=1,best=nums[0];\nfor(int i=0;i<nums.size();++i){\n if(prefix==0)prefix=1; if(suffix==0)suffix=1;\n prefix*=nums[i]; suffix*=nums[nums.size()-1-i];\n best=max(best,max(prefix,suffix));\n}",
   "prefix = suffix = 1\nbest = nums[0]\nfor i in range(len(nums)):\n    if prefix == 0: prefix = 1\n    if suffix == 0: suffix = 1\n    prefix *= nums[i]\n    suffix *= nums[-1-i]\n    best = max(best, prefix, suffix)")),
  new Pattern("sliding-window","Sliding Window","O(n) time · O(1) space","Maintains a moving range so repeated work is removed.","Best for contiguous subarrays or substrings with a size/condition.",code(
   "int left = 0;\nfor (int right = 0; right < a.length; right++) {\n  while (!valid(left, right)) left++;\n  best = Math.max(best, right - left + 1);\n}",
   "int left = 0;\nfor (int right = 0; right < a.size(); ++right) {\n  while (!valid(left, right)) ++left;\n  best = max(best, right-left+1);\n}",
   "left = 0\nfor right in range(len(a)):\n    while not valid(left, right):\n        left += 1\n    best = max(best, right-left+1)")),
  new Pattern("two-pointers","Two Pointers","O(n) time · O(1) space","Moves two indices strategically instead of checking every pair.","Best for sorted data, pairs, palindromes, or in-place partitioning.",code(
   "int left=0, right=a.length-1;\nwhile(left<right){\n  if(matches(a[left],a[right])) return true;\n  if(tooSmall()) left++; else right--;\n}",
   "int l=0,r=a.size()-1;\nwhile(l<r){\n  if(matches(a[l],a[r])) return true;\n  tooSmall()?++l:--r;\n}",
   "left, right = 0, len(a)-1\nwhile left < right:\n    if matches(a[left], a[right]): return True\n    if too_small(): left += 1\n    else: right -= 1")),
  new Pattern("hashing","Hash Map / Set","O(n) time · O(n) space","Trades memory for constant-time lookups and frequency tracking.","Best for complements, duplicates, grouping, and counting.",code(
   "Map<Integer,Integer> seen=new HashMap<>();\nfor(int i=0;i<a.length;i++){\n  if(seen.containsKey(target-a[i])) return new int[]{seen.get(target-a[i]),i};\n  seen.put(a[i],i);\n}",
   "unordered_map<int,int> seen;\nfor(int i=0;i<a.size();++i){\n  if(seen.count(target-a[i])) return {seen[target-a[i]],i};\n  seen[a[i]]=i;\n}",
   "seen = {}\nfor i, value in enumerate(a):\n    if target-value in seen: return [seen[target-value], i]\n    seen[value] = i")),
  new Pattern("binary-search","Binary Search","O(log n) time · O(1) space","Discards half of a monotonic search space each step.","Best for sorted values or answer spaces with a true/false boundary.",code(
   "int lo=0,hi=a.length-1;\nwhile(lo<=hi){\n int mid=lo+(hi-lo)/2;\n if(a[mid]==target)return mid;\n if(a[mid]<target)lo=mid+1;else hi=mid-1;\n}",
   "int lo=0,hi=a.size()-1;\nwhile(lo<=hi){int mid=lo+(hi-lo)/2;if(a[mid]==target)return mid;a[mid]<target?lo=mid+1:hi=mid-1;}",
   "lo, hi = 0, len(a)-1\nwhile lo <= hi:\n    mid = lo + (hi-lo)//2\n    if a[mid] == target: return mid\n    if a[mid] < target: lo = mid+1\n    else: hi = mid-1")),
  new Pattern("stack","Monotonic Stack","O(n) time · O(n) space","Keeps only unresolved candidates, so every element is pushed and popped at most once.","Best for next greater/smaller element, histogram, and bracket problems.",code(
   "Deque<Integer> stack=new ArrayDeque<>();\nfor(int i=0;i<a.length;i++){\n  while(!stack.isEmpty() && a[stack.peek()]<a[i]) answer[stack.pop()]=a[i];\n  stack.push(i);\n}",
   "stack<int> st;\nfor(int i=0;i<a.size();++i){\n  while(!st.empty() && a[st.top()]<a[i]){answer[st.top()]=a[i];st.pop();}\n  st.push(i);\n}",
   "stack = []\nfor i, value in enumerate(a):\n    while stack and a[stack[-1]] < value:\n        answer[stack.pop()] = value\n    stack.append(i)")),
  new Pattern("graph-traversal","BFS / DFS","O(V + E) time · O(V) space","Marks each node once and explores every connection once.","Best for graphs, grids, connected components, and shortest unweighted paths.",code(
   "Queue<Integer> q=new ArrayDeque<>();\nq.add(start); visited[start]=true;\nwhile(!q.isEmpty()){\n int node=q.remove();\n for(int next:graph[node]) if(!visited[next]){visited[next]=true;q.add(next);}\n}",
   "queue<int> q; q.push(start); seen[start]=true;\nwhile(!q.empty()){int node=q.front();q.pop();for(int next:g[node])if(!seen[next]){seen[next]=true;q.push(next);}}",
   "q = deque([start]); seen = {start}\nwhile q:\n    node = q.popleft()\n    for nxt in graph[node]:\n        if nxt not in seen:\n            seen.add(nxt); q.append(nxt)")),
  new Pattern("merge-sort","Merge Sort","O(n log n) time · O(n) space","Splits the input into halves, sorts each half recursively, and merges two sorted halves in linear time.","Best when stable, predictable O(n log n) sorting is required.",code(
   "void mergeSort(int[] a,int lo,int hi){\n if(lo>=hi)return;\n int mid=lo+(hi-lo)/2;\n mergeSort(a,lo,mid); mergeSort(a,mid+1,hi);\n merge(a,lo,mid,hi);\n}",
   "void mergeSort(vector<int>& a,int lo,int hi){\n if(lo>=hi)return;\n int mid=lo+(hi-lo)/2;\n mergeSort(a,lo,mid); mergeSort(a,mid+1,hi);\n merge(a,lo,mid,hi);\n}",
   "def merge_sort(a):\n    if len(a) <= 1: return a\n    mid = len(a)//2\n    left = merge_sort(a[:mid])\n    right = merge_sort(a[mid:])\n    return merge(left, right)")),
  new Pattern("linked-list","Linked List Pointers","O(n) time · O(1) space","Rewires or advances node references without shifting array elements.","Best for reversal, cycle detection, middle node, and nth-from-end problems.",code(
   "ListNode prev=null,cur=head;\nwhile(cur!=null){ListNode next=cur.next;cur.next=prev;prev=cur;cur=next;}\nreturn prev;",
   "ListNode *prev=nullptr,*cur=head;\nwhile(cur){auto next=cur->next;cur->next=prev;prev=cur;cur=next;}\nreturn prev;",
   "prev, cur = None, head\nwhile cur:\n    nxt = cur.next\n    cur.next = prev\n    prev, cur = cur, nxt\nreturn prev")),
  new Pattern("tree-traversal","Tree DFS / BFS","O(n) time · O(h) space","Visits each tree node once while recursion or a queue tracks the frontier.","Use DFS for paths/depth/subtrees and BFS for levels/nearest nodes.",code(
   "int dfs(TreeNode node){\n if(node==null)return 0;\n return 1+Math.max(dfs(node.left),dfs(node.right));\n}",
   "int dfs(TreeNode* node){if(!node)return 0;return 1+max(dfs(node->left),dfs(node->right));}",
   "def dfs(node):\n    if not node: return 0\n    return 1 + max(dfs(node.left), dfs(node.right))")),
  new Pattern("backtracking","Backtracking","O(b^d) time · O(d) space","Makes a choice, explores it, then undoes the choice to try another branch.","Best for all subsets, permutations, N-Queens, Sudoku, and Word Search.",code(
   "void backtrack(int start,List<Integer> path){\n answer.add(new ArrayList<>(path));\n for(int i=start;i<a.length;i++){path.add(a[i]);backtrack(i+1,path);path.remove(path.size()-1);}\n}",
   "void backtrack(int start,vector<int>& path){ans.push_back(path);for(int i=start;i<a.size();i++){path.push_back(a[i]);backtrack(i+1,path);path.pop_back();}}",
   "def backtrack(start, path):\n    answer.append(path[:])\n    for i in range(start, len(a)):\n        path.append(a[i])\n        backtrack(i+1, path)\n        path.pop()")),
  new Pattern("divide-conquer","Divide & Conquer","O(n log n) time · O(log n) space","Splits independent subproblems, solves them recursively, and combines the results.","Best for independent halves, inversion counting, and partition-based algorithms.",code(
   "Result solve(int lo,int hi){\n if(lo==hi)return base(lo);\n int mid=lo+(hi-lo)/2;\n return combine(solve(lo,mid),solve(mid+1,hi));\n}",
   "Result solve(int lo,int hi){if(lo==hi)return base(lo);int mid=lo+(hi-lo)/2;return combine(solve(lo,mid),solve(mid+1,hi));}",
   "def solve(lo, hi):\n    if lo == hi: return base(lo)\n    mid = (lo+hi)//2\n    return combine(solve(lo,mid), solve(mid+1,hi))")),
  new Pattern("heap","Heap / Priority Queue","O(n log k) time · O(k) space","Maintains only the best k candidates instead of sorting the whole input.","Best for Top K, Kth element, repeated best choice, scheduling, and streams.",code(
   "PriorityQueue<Integer> heap=new PriorityQueue<>();\nfor(int value:a){heap.offer(value);if(heap.size()>k)heap.poll();}\nreturn heap.peek();",
   "priority_queue<int,vector<int>,greater<int>> heap;\nfor(int x:a){heap.push(x);if(heap.size()>k)heap.pop();}\nreturn heap.top();",
   "heap = []\nfor value in a:\n    heappush(heap, value)\n    if len(heap) > k: heappop(heap)\nreturn heap[0]")),
  new Pattern("trie","Trie","O(L) per operation · O(total characters) space","Walks one edge per character, making prefix operations independent of dictionary size.","Best for prefix search, autocomplete, wildcard dictionaries, and many-word lookup.",code(
   "TrieNode node=root;\nfor(char ch:word.toCharArray()){int i=ch-'a';if(node.child[i]==null)node.child[i]=new TrieNode();node=node.child[i];}\nnode.end=true;",
   "TrieNode* node=root;for(char ch:word){int i=ch-'a';if(!node->child[i])node->child[i]=new TrieNode();node=node->child[i];}node->end=true;",
   "node = root\nfor ch in word:\n    node = node.children.setdefault(ch, TrieNode())\nnode.end = True")),
  new Pattern("dynamic-programming","Dynamic Programming","O(number of states) time · O(number of states) space","Stores repeated states so each subproblem is solved once.","Best for count/min/max/possible questions with overlapping subproblems.",code(
   "int[] dp=new int[n+1];dp[0]=0;dp[1]=1;\nfor(int i=2;i<=n;i++)dp[i]=best(dp[i-1],dp[i-2]);\nreturn dp[n];",
   "vector<int> dp(n+1);dp[1]=1;for(int i=2;i<=n;i++)dp[i]=best(dp[i-1],dp[i-2]);return dp[n];",
   "dp = [0]*(n+1)\ndp[1] = 1\nfor i in range(2,n+1): dp[i] = best(dp[i-1],dp[i-2])\nreturn dp[n]")),
  new Pattern("greedy","Greedy","O(n log n) time · O(1) space","Sorts choices and commits to the locally best valid option without revisiting it.","Best for interval scheduling, events, deadlines, and provable local-choice problems.",code(
   "Arrays.sort(intervals,Comparator.comparingInt(x->x[1]));\nint end=Integer.MIN_VALUE,count=0;\nfor(int[] in:intervals)if(in[0]>=end){count++;end=in[1];}",
   "sort(a.begin(),a.end(),[](auto&x,auto&y){return x[1]<y[1];});int end=INT_MIN,count=0;for(auto& in:a)if(in[0]>=end){count++;end=in[1];}",
   "intervals.sort(key=lambda x:x[1])\nend=float('-inf'); count=0\nfor start,finish in intervals:\n    if start>=end: count+=1; end=finish")),
  new Pattern("sorting","Sorting / Partitioning","O(n log n) average time · O(log n) space","Orders data to expose structure or partitions around a pivot.","Choose Merge Sort for stability, Quick Sort for in-place average speed, or counting/radix for bounded keys.",code(
   "Arrays.sort(a); // Dual-pivot quicksort for primitives\n// Use a custom comparator for objects.",
   "sort(a.begin(),a.end()); // introsort: quicksort + heapsort + insertion sort",
   "a.sort()  # Timsort: stable, O(n log n) worst case")),
  new Pattern("topological-sort","Topological Sort","O(V + E) time · O(V) space","Processes zero-in-degree nodes to produce a valid dependency order.","Best for prerequisites, build order, task scheduling, and directed-cycle detection.",code(
   "Queue<Integer> q=new ArrayDeque<>();for(int i=0;i<n;i++)if(indegree[i]==0)q.add(i);while(!q.isEmpty()){int u=q.remove();order.add(u);for(int v:g[u])if(--indegree[v]==0)q.add(v);}",
   "queue<int> q;for(int i=0;i<n;i++)if(!indegree[i])q.push(i);while(!q.empty()){int u=q.front();q.pop();order.push_back(u);for(int v:g[u])if(--indegree[v]==0)q.push(v);}",
   "q=deque(i for i in range(n) if indegree[i]==0)\nwhile q:\n    u=q.popleft(); order.append(u)\n    for v in graph[u]:\n        indegree[v]-=1\n        if indegree[v]==0:q.append(v)")),
  new Pattern("union-find","Union Find","O(E α(V)) time · O(V) space","Uses parent links, path compression, and rank to maintain connected components.","Best for dynamic connectivity, redundant edges, Kruskal MST, and undirected cycles.",code(
   "int find(int x){return parent[x]==x?x:(parent[x]=find(parent[x]));}\nvoid union(int a,int b){a=find(a);b=find(b);if(a!=b)parent[b]=a;}",
   "int find(int x){return parent[x]==x?x:parent[x]=find(parent[x]);}void unite(int a,int b){a=find(a);b=find(b);if(a!=b)parent[b]=a;}",
   "def find(x):\n    if parent[x]!=x: parent[x]=find(parent[x])\n    return parent[x]\ndef union(a,b):\n    parent[find(b)]=find(a)")),
  new Pattern("dijkstra","Dijkstra","O((V + E) log V) time · O(V + E) space","Repeatedly expands the closest unsettled vertex through a min-heap.","Best for non-negative weighted shortest paths; lazy heaps may store O(E) entries.",code(
   "PriorityQueue<State> pq=new PriorityQueue<>();pq.add(new State(start,0));while(!pq.isEmpty()){State cur=pq.poll();if(cur.d!=dist[cur.v])continue;for(Edge e:g[cur.v])if(cur.d+e.w<dist[e.to]){dist[e.to]=cur.d+e.w;pq.add(new State(e.to,dist[e.to]));}}",
   "priority_queue<State,vector<State>,greater<State>> pq;pq.push({0,start});while(!pq.empty()){auto [d,u]=pq.top();pq.pop();if(d!=dist[u])continue;for(auto [v,w]:g[u])if(d+w<dist[v]){dist[v]=d+w;pq.push({dist[v],v});}}",
   "pq=[(0,start)]\nwhile pq:\n    d,u=heappop(pq)\n    if d!=dist[u]: continue\n    for v,w in graph[u]:\n        if d+w<dist[v]: dist[v]=d+w; heappush(pq,(dist[v],v))"))
 );
 public static Pattern byId(String id){return ALL.stream().filter(p->p.id().equals(id)).findFirst().orElse(ALL.get(0));}
}
