package dev.algolens.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CodeAnalyzerTest {
 private final CodeAnalyzer analyzer=new CodeAnalyzer();

 @ParameterizedTest(name="{0}")
 @MethodSource("solutions")
 void recognizesCommonPatterns(String name,String code,String pattern,String time,String space){
  var result=analyzer.analyze(code);
  assertEquals(pattern,result.primaryPattern());
  assertEquals(time,result.timeComplexity());
  assertEquals(space,result.spaceComplexity());
  assertTrue(result.patternIds().contains(pattern));
  assertTrue(result.patternIds().size() >= 3,"Detected code should include two related patterns");
 }

 static Stream<Arguments> solutions(){return Stream.of(
  Arguments.of("Java binary search","int search(int[] a,int x){int left=0,right=a.length-1;while(left<=right){int mid=(left+right)/2;if(a[mid]==x)return mid;if(a[mid]<x)left=mid+1;else right=mid-1;}return -1;}","binary-search","O(log n)","O(1)"),
  Arguments.of("Java prefix suffix product","int prefix=1,suffix=1,max=nums[0],n=nums.length;for(int i=0;i<n;i++){if(prefix==0)prefix=1;if(suffix==0)suffix=1;prefix *= nums[i];suffix *= nums[n-i-1];max=Math.max(max,Math.max(prefix,suffix));}","prefix-suffix-product","O(n)","O(1)"),
  Arguments.of("Java merge sort","void mergeSort(int[] a){if(a.length<2)return;int[] left=Arrays.copyOfRange(a,0,a.length/2);int[] right=Arrays.copyOfRange(a,a.length/2,a.length);mergeSort(left);mergeSort(right);merge(a,left,right);}","merge-sort","O(n log n)","O(n)"),
  Arguments.of("Java dynamic programming","int[] dp=new int[n+1];for(int i=2;i<=n;i++){dp[i]=Math.max(dp[i-1],dp[i-2]+nums[i-1]);}","dynamic-programming","O(n)","O(n)"),
  Arguments.of("Java indexed sliding window","Map<Character,Integer> lastSeen=new HashMap<>();int left=0,maximumLength=0;for(int right=0;right<text.length();right++){char current=text.charAt(right);if(lastSeen.containsKey(current)){left=Math.max(left,lastSeen.get(current)+1);}lastSeen.put(current,right);maximumLength=Math.max(maximumLength,right-left+1);}","sliding-window","O(n)","O(k)"),
  Arguments.of("C++ two pointers","bool pairSum(vector<int>& a,int target){int left=0,right=a.size()-1;while(left<right){int sum=a[left]+a[right];if(sum==target)return true;if(sum<target)left++;else right--;}return false;}","two-pointers","O(n)","O(1)"),
  Arguments.of("C++ sliding window","int left=0,best=0;for(int right=0;right<a.size();right++){while(sum>limit){sum-=a[left++];}best=max(best,right-left+1);}","sliding-window","O(n)","O(1)"),
  Arguments.of("C++ heap","priority_queue<int,vector<int>,greater<int>> pq;for(int x:nums){pq.push(x);if(pq.size()>k)pq.pop();}","heap","O(n log k)","O(k)"),
  Arguments.of("C++ union find","class UnionFind{vector<int> parent,rank;int findParent(int x){return parent[x]==x?x:parent[x]=findParent(parent[x]);}void union(int a,int b){}};","union-find","O(E α(V))","O(V)"),
  Arguments.of("Python hashing","seen = {}\nfor i, x in enumerate(nums):\n    if target-x in seen: return [seen[target-x], i]\n    seen[x] = i","hashing","O(n)","O(n)"),
  Arguments.of("Python BFS","visited = {start}\nq = deque([start])\nwhile q:\n    node = q.popleft()\n    for nxt in graph[node]:\n        if nxt not in visited:\n            visited.add(nxt); q.append(nxt)","graph-traversal","O(V + E)","O(V)"),
  Arguments.of("Python backtracking","def backtrack(path):\n    if done(path): answer.append(path[:]); return\n    for choice in choices:\n        path.append(choice); backtrack(path); path.pop()","backtracking","O(b^d)","O(d)"),
  Arguments.of("Python trie","class TrieNode: pass\ndef insert(word):\n    node = root\n    for ch in word:\n        node = node.children.setdefault(ch, TrieNode())","trie","O(L)","O(total characters)")
 );}
}
