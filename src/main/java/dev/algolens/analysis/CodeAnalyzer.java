package dev.algolens.analysis;

import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class CodeAnalyzer {
 public record Result(String timeComplexity,String spaceComplexity,String verdict,String primaryPattern,List<String> patternIds,List<String> signals){}

 public Result analyze(String source){
  String s=source.toLowerCase(Locale.ROOT).replace("\r","");
  List<String> signals=new ArrayList<>(); List<String> patterns=new ArrayList<>();
  int loops=count(s,"for (")+count(s,"for(")+count(s,"while (")+count(s,"while(");
  boolean nested=s.matches("(?s).*(for|while)\\s*\\([^)]*\\).*\\{?.*(for|while)\\s*\\(.*");
  boolean mergeSort=s.contains("mergesort(")||s.contains("merge_sort(")||s.contains("copyofrange")&&s.contains("merge(");
  boolean quickSort=s.contains("quicksort(")||s.contains("quick_sort(")||s.contains("partition(")&&s.contains("pivot");
  boolean librarySort=s.contains("arrays.sort")||s.contains("collections.sort")||s.contains("std::sort")||s.contains("sorted(")||s.contains(".sort(");
  boolean binary=!mergeSort&&!quickSort&&s.matches("(?s).*\\bmid\\b.*")&&(word(s,"left")||word(s,"low")||word(s,"lo"))&&(word(s,"right")||word(s,"high")||word(s,"hi"));
  boolean trie=s.contains("trienode")||s.contains("class trie")||s.contains("startswith(")||s.contains("children[26]")||s.contains("children = {}");
  boolean heap=s.contains("priorityqueue")||s.contains("priority_queue")||s.contains("heapq.")||s.contains("heappush(")||s.contains("heappop(");
  boolean unionFind=s.contains("unionfind")||s.contains("disjointset")||s.contains("findparent(")||s.contains("parent[")&&s.contains("union(");
  boolean topological=s.contains("indegree")||s.contains("topological")||s.contains("toposort");
  boolean dijkstra=(heap||s.contains("priority"))&&(s.contains("distance")||s.contains("dist[")||s.contains("shortest"));
  boolean graph=dijkstra||unionFind||topological||s.contains("dfs(")||s.contains("bfs(")||s.contains("visited")||s.contains("adjacency")||s.contains("graph[")&&s.contains("queue");
  boolean tree=s.contains("treenode")||s.contains("binarytree")||s.contains("root.left")||s.contains("root.right")||s.contains("node.left")&&s.contains("node.right");
  boolean linked=s.contains("listnode")||s.contains("linkedlist")||s.contains("node.next")||s.contains("head.next");
  boolean backtracking=s.contains("backtrack(")||s.contains("backtracking")||(s.contains("permut")||s.contains("subset")||s.contains("nqueens"))&&(s.contains("remove(")||s.contains("pop(")||s.contains("used["));
  boolean prefixSuffixProduct=word(s,"prefix")&&word(s,"suffix")&&
   (s.matches("(?s).*\\bprefix\\s*\\*=.*")||s.contains("prefix = prefix *"))&&
   (s.matches("(?s).*\\bsuffix\\s*\\*=.*")||s.contains("suffix = suffix *"))&&
   (s.contains("n-i-1")||s.contains("n - i - 1")||s.contains("length-i-1")||s.contains("length - i - 1"));
  boolean kadane=((s.contains("current_sum")||s.contains("currentsum")||s.contains("currentmax"))&&(s.contains("maximum_sum")||s.contains("maxsum")||s.contains("globalmax")))||
   word(s,"current")&&word(s,"best")&&(s.contains("math.max(")||s.contains("max("))&&s.contains("current +");
  boolean dp=!prefixSuffixProduct&&(kadane||s.contains("dp[")||s.contains("memo[")||s.contains("@lru_cache")||s.contains("memoization")||s.contains("tabulation"));
  boolean greedy=s.contains("greedy")||librarySort&&(s.contains("interval")||s.contains("endtime")||s.contains("deadline")||s.contains("activities"));
  boolean divide=mergeSort||quickSort||s.contains("divideandconquer")||s.contains("divide_and_conquer");
  boolean hash=s.contains("hashmap")||s.contains("hashset")||s.contains("unordered_map")||s.contains("unordered_set")||s.contains("dictionary<")||s.contains("counter(")||s.contains("defaultdict(")||s.contains("seen = {");
  boolean stack=s.contains("stack<")||s.contains("deque<")||s.contains("arraydeque")||s.contains("stack = []")&&s.contains("pop(");
  boolean shrinkingWindow=word(s,"left")&&word(s,"right")&&s.contains("for")&&s.contains("while")&&
   (s.contains("left++")||s.contains("++left")||s.contains("left += 1"));
  boolean indexedWindow=hash&&word(s,"left")&&word(s,"right")&&s.contains("for")&&
   (s.contains("right-left+1")||s.contains("right - left + 1"))&&
   (s.contains("lastseen")||s.contains("last_seen")||s.contains("last seen"));
  boolean window=s.contains("windowsum")||s.contains("window_sum")||s.contains("window")&&s.contains("left")&&s.contains("right")||shrinkingWindow||indexedWindow;
  boolean twoPointers=!binary&&!window&&((word(s,"left")&&word(s,"right"))||(word(s,"slow")&&word(s,"fast")));

  String time; String space; String verdict;
  if(dijkstra){time="O((V + E) log V)";space="O(V + E)";add(patterns,"dijkstra");signals.add("Weighted shortest-path relaxation with a priority queue");verdict="Each edge relaxation is paired with logarithmic priority-queue work; lazy duplicate heap entries can require O(E) queue space.";}
  else if(unionFind){time="O(E α(V))";space="O(V)";add(patterns,"union-find");signals.add("Parent/rank component tracking detected");verdict="Path compression and union by rank make connectivity operations nearly constant time.";}
  else if(topological){time="O(V + E)";space="O(V)";add(patterns,"topological-sort");signals.add("In-degree based dependency ordering detected");verdict="Every vertex and dependency edge is processed once.";}
  else if(graph){time="O(V + E)";space="O(V)";add(patterns,"graph-traversal");signals.add("Graph traversal with visited state detected");verdict="Every reachable vertex and connecting edge is processed once.";}
  else if(mergeSort){time="O(n log n)";space="O(n)";add(patterns,"merge-sort");signals.add("Recursive split and linear merge detected");verdict="There are log n split levels and n merge work per level.";}
  else if(quickSort){time="O(n log n) avg";space="O(log n)";add(patterns,"sorting");signals.add("Pivot partitioning detected");verdict="Balanced partitions give O(n log n) average time; poor pivots can degrade to O(n²).";}
  else if(binary){time="O(log n)";space="O(1)";add(patterns,"binary-search");signals.add("Search interval is halved each iteration");verdict="The monotonic search space is halved on every step.";}
  else if(trie){time="O(L)";space="O(total characters)";add(patterns,"trie");signals.add("Character-by-character prefix navigation detected");verdict="Trie work depends on word length rather than the number of stored words.";}
  else if(backtracking){time="O(b^d)";space="O(d)";add(patterns,"backtracking");signals.add("Choose, explore, and undo structure detected");verdict="The search explores a branching decision tree; pruning is the main optimization.";}
  else if(kadane){time="O(n)";space="O(1)";add(patterns,"dynamic-programming");signals.add("Rolling best/current state detected");verdict="Kadane’s algorithm keeps only the best subarray ending at the current index.";}
  else if(dp){boolean matrix=s.contains("dp[")&&(s.contains("][")||s.contains("[][]"));time=matrix?"O(n × m)":"O(n)";space=matrix?"O(n × m)":"O(n)";add(patterns,"dynamic-programming");signals.add("Reusable state table or memo detected");verdict="Previously solved states are reused instead of recomputed.";}
  else if(prefixSuffixProduct){time="O(n)";space="O(1)";add(patterns,"prefix-suffix-product");signals.add("Forward and backward running products detected");verdict="Both directions are scanned in one loop, handling negative values and zero resets with constant extra state.";}
  else if(heap){time="O(n log k)";space="O(k)";add(patterns,"heap");signals.add("Priority-based selection detected");verdict="The heap maintains only the best k candidates instead of fully sorting everything.";}
  else if(tree){time="O(n)";space="O(h)";add(patterns,"tree-traversal");signals.add("Tree node navigation detected");verdict="Each node is visited once; recursion or the queue stores the active frontier.";}
  else if(linked){time="O(n)";space="O(1)";add(patterns,"linked-list");signals.add("Next-pointer navigation detected");verdict="The list is traversed once using pointer updates without array shifting.";}
  else if(greedy){time="O(n log n)";space="O(1)";add(patterns,"greedy");signals.add("Sort followed by irreversible local choices detected");verdict="Sorting establishes the choice order; each item is then considered once.";}
  else if(window){time="O(n)";space=hash?"O(k)":"O(1)";add(patterns,"sliding-window");signals.add("Contiguous moving range detected");verdict="Each boundary only moves forward, avoiding repeated range scans.";}
  else if(stack){time="O(n)";space="O(n)";add(patterns,"stack");signals.add("LIFO candidate tracking detected");verdict="Each element is pushed and popped at most once.";}
  else if(twoPointers){time="O(n)";space="O(1)";add(patterns,"two-pointers");signals.add("Coordinated pointer movement detected");verdict="The pointers eliminate candidates without checking every pair.";}
  else if(hash){time="O(n)";space="O(n)";add(patterns,"hashing");signals.add("Constant-average-time lookup detected");verdict="Extra memory replaces repeated searches with direct lookup.";}
  else if(librarySort){time="O(n log n)";space="O(log n)";add(patterns,"sorting");signals.add("General-purpose comparison sorting detected");verdict="Comparison sorting provides reliable O(n log n) performance.";}
  else if(divide){time="O(n log n)";space="O(log n)";add(patterns,"divide-conquer");signals.add("Independent recursive partitions detected");verdict="Independent halves are solved recursively and their results combined.";}
  else {time=nested?"O(n²)":loops>0?"O(n)":"O(1)";space="O(1)";signals.add(nested?"Nested iteration detected":loops>0?"Single-pass iteration detected":"No input-sized iteration detected");verdict=nested?"Repeated work is visible; compare the suggested patterns for a way to remove the inner scan.":"The code uses a simple input pass with constant auxiliary state.";}

  if(hash&&!patterns.contains("hashing"))add(patterns,"hashing");
  String primary=patterns.isEmpty()?"unclassified":patterns.get(0);
  addRelatedPatterns(patterns);
  return new Result(time,space,verdict,primary,patterns,signals);
 }

 private void addRelatedPatterns(List<String> patterns){
  if(patterns.isEmpty()){add(patterns,"hashing");add(patterns,"sorting");add(patterns,"binary-search");return;}
  Map<String,List<String>> related=Map.ofEntries(
   Map.entry("dijkstra",List.of("heap","graph-traversal")),Map.entry("graph-traversal",List.of("topological-sort","union-find")),
   Map.entry("topological-sort",List.of("graph-traversal","union-find")),Map.entry("union-find",List.of("graph-traversal","sorting")),
   Map.entry("binary-search",List.of("sorting","divide-conquer")),Map.entry("merge-sort",List.of("divide-conquer","sorting")),
   Map.entry("sorting",List.of("binary-search","heap")),Map.entry("heap",List.of("sorting","greedy")),
   Map.entry("trie",List.of("hashing","graph-traversal")),Map.entry("backtracking",List.of("dynamic-programming","graph-traversal")),
   Map.entry("dynamic-programming",List.of("greedy","backtracking")),Map.entry("greedy",List.of("sorting","heap")),
   Map.entry("tree-traversal",List.of("graph-traversal","stack")),Map.entry("linked-list",List.of("two-pointers","stack")),
   Map.entry("stack",List.of("tree-traversal","hashing")),Map.entry("sliding-window",List.of("hashing","two-pointers")),
   Map.entry("two-pointers",List.of("sorting","binary-search")),Map.entry("hashing",List.of("sorting","binary-search")),
   Map.entry("prefix-suffix-product",List.of("dynamic-programming","two-pointers")),Map.entry("divide-conquer",List.of("merge-sort","binary-search"))
  );
  for(String id:related.getOrDefault(patterns.get(0),List.of("hashing","sorting")))add(patterns,id);
  for(String id:List.of("hashing","sorting","binary-search")){if(patterns.size()>=3)break;add(patterns,id);}
 }

 private void add(List<String> values,String value){if(!values.contains(value))values.add(value);}
 private boolean word(String source,String token){return source.matches("(?s).*\\b"+token+"\\b.*");}
 private int count(String s,String token){int n=0,p=0;while((p=s.indexOf(token,p))>=0){n++;p+=token.length();}return n;}
}
