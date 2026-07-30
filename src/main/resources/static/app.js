const $=s=>document.querySelector(s), $$=s=>[...document.querySelectorAll(s)];
function readSavedUser(){try{return JSON.parse(globalThis.localStorage?.getItem('algolens_user')||'null')}catch{return null}}
function saveUser(user){try{globalThis.localStorage?.setItem('algolens_user',JSON.stringify(user))}catch{}}
const state={user:readSavedUser(),mode:'register'};
const starterCodes={JAVA:``,CPP:``,PYTHON:``};
const samples={
 hashing:{name:'Hash Map — one pass',complexity:'O(n) time',reason:'At each number, ask: “Have I already seen its complement?” A hash map answers in constant time, removing the second loop entirely.',trade:'Uses O(n) extra memory to reduce runtime from quadratic to linear.',code:`Map<Integer, Integer> seen = new HashMap<>();
for (int i = 0; i < nums.length; i++) {
    int need = target - nums[i];
    if (seen.containsKey(need))
        return new int[] {seen.get(need), i};
    seen.put(nums[i], i);
}`},
 brute:{name:'Brute force — nested scan',complexity:'O(n²) time',reason:'Try every pair and return when their sum matches the target. It is easy to derive and useful as a correctness baseline.',trade:'Uses constant extra space, but the number of comparisons grows quadratically.',code:`for (int i = 0; i < nums.length; i++) {
    for (int j = i + 1; j < nums.length; j++) {
        if (nums[i] + nums[j] == target)
            return new int[] {i, j};
    }
}`},
 'two-pointers':{name:'Two Pointers — sorted',complexity:'O(n log n) time',reason:'After sorting, the pair sum tells you exactly which pointer to move. Preserve original indices if the problem asks for them.',trade:'Very low auxiliary space, but sorting changes order and dominates runtime.',code:`Arrays.sort(nums);
int left = 0, right = nums.length - 1;
while (left < right) {
    int sum = nums[left] + nums[right];
    if (sum == target) return new int[] {left, right};
    if (sum < target) left++; else right--;
}`}}
function toast(msg){const t=$('#toast');t.textContent=msg;t.classList.add('show');setTimeout(()=>t.classList.remove('show'),2600)}
function chartKind(c){if(c.includes('b^')||c.includes('2^'))return'exponential';if(c.includes('n log')||c.includes('log V'))return'nlogn';if(c.includes('²'))return'quadratic';if(c.includes('log'))return'logarithmic';return'linear'}
function drawChart(kind='linear') {const c=$('#complexityChart'),dpr=devicePixelRatio||1,w=c.clientWidth,h=c.clientHeight;c.width=w*dpr;c.height=h*dpr;const x=c.getContext('2d');x.scale(dpr,dpr);x.clearRect(0,0,w,h);x.strokeStyle='#292630';x.lineWidth=1;for(let i=1;i<5;i++){x.beginPath();x.moveTo(0,i*h/5);x.lineTo(w,i*h/5);x.stroke()}const plot=(color,fn,width=2)=>{x.beginPath();for(let i=0;i<=50;i++){let n=i/50,y=Math.min(1,fn(n));let px=i*w/50,py=h-8-y*(h-24);i?x.lineTo(px,py):x.moveTo(px,py)}x.strokeStyle=color;x.lineWidth=width;x.stroke()};plot('#4b4652',n=>n*n*.95,1.5);const fn=kind==='exponential'?n=>(Math.pow(2,n*5)-1)/31:kind==='quadratic'?n=>n*n*.95:kind==='nlogn'?n=>n*Math.log2(1+n*8)/3.5:kind==='logarithmic'?n=>Math.log2(1+n*15)/5:n=>n*.72;plot('#a987ff',fn,2.2)}
function complexityLabels(result){const p=result.primaryPattern,t=result.timeComplexity,s=result.spaceComplexity;const time=p==='trie'?'Word-length traversal':p==='backtracking'?'Branching decision tree':p==='union-find'?'Near-constant connectivity':p==='dijkstra'?'Heap-based edge relaxation':t.includes('V')?'Vertices + edges':t.includes('n log')?'Linearithmic growth':t.includes('log')?'Logarithmic growth':t.includes('²')?'Quadratic growth':t.includes('n')?'Linear growth':'Constant time';const space=s==='O(1)'?'Constant auxiliary space':p==='graph-traversal'||p==='topological-sort'||p==='dijkstra'?'Graph frontier and visited state':p==='trie'?'Stored character nodes':p==='backtracking'?'Recursion path':p==='hashing'?'Hash-based storage':'Input-sized auxiliary storage';return{time,space}}
function setPattern(id){$$('.pattern-card').forEach(c=>c.classList.toggle('active',c.dataset.pattern===id));const p=samples[id]||samples.hashing;$('#detailName').textContent=p.name;$('#detailComplexity').textContent=p.complexity;$('#detailReason').textContent=p.reason;$('#patternCode').textContent=p.code;$('.tradeoff p').textContent=p.trade;$('#detailTag').textContent=id==='hashing'?'RECOMMENDED APPROACH':id==='brute'?'BASELINE APPROACH':'ALTERNATIVE APPROACH'}
$$('.pattern-card').forEach(c=>c.addEventListener('click',()=>setPattern(c.dataset.pattern)));
function renderPatterns(patterns,time){
 const heading=$('.patterns-section .section-title h2');
 if(!patterns?.length){heading.textContent='No confident pattern detected';$('#patternList').innerHTML='<div class="pattern-card muted"><div><h3>General code structure</h3><p>AlgoLens will not guess an unrelated algorithm without enough evidence.</p></div></div>';$('.pattern-detail').hidden=true;return;}
 $('.pattern-detail').hidden=false;heading.textContent=patterns.length===1?'Detected pattern':'Detected & supporting patterns';
 const all=[...patterns];
 const role=(p,i)=>i===0?'DETECTED':/^RECOMMENDED:/i.test(p.clue||'')?'MOST EFFICIENT':/^BRUTE FORCE:/i.test(p.clue||'')?'BRUTE FORCE':'SUPPORTING / RELATED';
 $('#patternList').innerHTML=all.map((p,i)=>`<button class="pattern-card ${i===0?'active':''}" data-pattern="${p.id}"><span class="pattern-icon">${i+1}</span><div><span class="tag ${role(p,i)==='MOST EFFICIENT'?'recommended':'alternative'}">${role(p,i)}</span><h3>${p.name}</h3><p>${p.clue||p.reason}</p><small>${p.complexity}</small></div><b>›</b></button>`).join('');
 const show=(p,i)=>{$$('.pattern-card').forEach(c=>c.classList.toggle('active',c.dataset.pattern===p.id));$('#detailTag').textContent=role(p,i)+' APPROACH';$('#detailName').textContent=p.name;$('#detailComplexity').textContent=p.complexity;$('#detailReason').textContent=p.reason;$('#patternCode').textContent=p.code;$('.tradeoff p').textContent=`Compare its time and memory trade-offs with the other applicable approaches. Submitted-code time: ${time}.`};
 $$('.pattern-card').forEach((card,i)=>card.onclick=()=>show(all[i],i));show(all[0],0);
}
function updateLineNumbers(){
  const val = $('#codeInput').value;
  const lines = val.split('\n').length;
  const gutter = $('.lines');
  let html = '';
  for(let i=1; i<=Math.max(1, lines); i++){
    html += i + '<br>';
  }
  gutter.innerHTML = html;
}
async function loadHistory() {
  const section = $('#history');
  if (!state.user) {
    section.style.display = 'none';
    return;
  }
  section.style.display = 'block';
  try {
    const r = await fetch(`/api/history/${state.user.id}`);
    if (r.ok) {
      const history = await r.json();
      const container = $('#history');
      const titleHtml = `<div class="section-title"><div><span class="section-no">03</span><div><span>YOUR PROGRESS</span><h2>Recent analyses</h2></div></div></div>`;
      if (history.length === 0) {
        container.innerHTML = titleHtml + `<div style="color:var(--muted);font-size:12px;padding:24px;text-align:center;border:1px dashed var(--line);background:var(--surface);width:100%">No analyses yet. Submit a solution to see it here!</div>`;
      } else {
        container.innerHTML = titleHtml + history.map(h => {
          const date = new Date(h.createdAt);
          const timeStr = date.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
          const dateStr = date.toLocaleDateString([], {month: 'short', day: 'numeric'});
          return `<div class="history-row" style="margin-bottom:8px;">
            <span class="history-icon">#</span>
            <div><strong>${h.primaryPattern.replace(/-/g, ' ').toUpperCase()}</strong><small>${state.user.language}</small></div>
            <span>${h.timeComplexity}</span>
            <span>${h.spaceComplexity}</span>
            <time>${dateStr} ${timeStr}</time>
          </div>`;
        }).join('');
      }
    }
  } catch (err) {
    console.error('Failed to load history', err);
  }
}
async function analyze(){const btn=$('#analyzeBtn'),code=$('#codeInput').value;if(!code.trim())return toast('Paste a solution first.');btn.disabled=true;btn.firstChild.textContent='◌';let result=null;let fromServer=false;if(state.user){try{const r=await fetch('/api/analyze',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({userId:state.user.id,sourceCode:code})});if(r.ok){result=await r.json();fromServer=true;setTimeout(loadHistory, 1000);}else{let msg='Analysis failed';try{const err=await r.json();msg=err.message||msg}catch{}btn.disabled=false;btn.firstChild.textContent='✦';toast(msg);return}}catch(err){toast('Network error — using local analysis.');}}
 if(!result){const lower=code.toLowerCase(),nested=(lower.match(/for\s*\(/g)||[]).length+(lower.match(/while\s*\(/g)||[]).length>1,hash=/hashmap|hashset|unordered_map|dict\s*\(/.test(lower),binary=/mid/.test(lower)&&/(left|low|lo)/.test(lower)&&/(right|high|hi)/.test(lower);result={timeComplexity:binary?'O(log n)':nested?'O(n²)':/sort\s*\(/.test(lower)?'O(n log n)':'O(n)',spaceComplexity:hash?'O(n)':'O(1)',verdict:binary?'The search interval is halved on every step, giving logarithmic time.':nested?'Repeated work is visible. Compare the optimized patterns below to remove the inner scan.':'Excellent work. This solution is already close to the optimal shape for a single-pass problem.',signals:[binary?'Search space is halved each iteration':nested?'Nested iteration detected':'Single-pass iteration detected',hash?'Hash lookup trades memory for speed':'Constant auxiliary memory detected'],analysisSource:'CLIENT',patterns:[]}}
 await new Promise(r=>setTimeout(r,500));const labels=complexityLabels(result);$('#timeValue').textContent=result.timeComplexity;$('#spaceValue').textContent=result.spaceComplexity;$('#timeLabel').textContent=labels.time;$('#spaceLabel').textContent=labels.space;$('#verdictText').textContent=result.verdict;$('#signals').innerHTML=result.signals.map(s=>`<span>${s}</span>`).join('');renderPatterns(result.patterns||[],result.timeComplexity);drawChart(chartKind(result.timeComplexity));btn.disabled=false;btn.firstChild.textContent='✦';toast(fromServer?result.analysisSource==='AI'?'AI analysis complete. Applicable approaches are ranked.':'AI unavailable — showing rule-based analysis.':'Demo analysis complete. Sign in for AI analysis.');$('#results').scrollIntoView({behavior:'smooth',block:'nearest'})}
$('#analyzeBtn').addEventListener('click',analyze);
$('#codeInput').addEventListener('input', updateLineNumbers);
$('#codeInput').addEventListener('keydown',e=>{if((e.metaKey||e.ctrlKey)&&e.key==='Enter')analyze();if(e.key==='Tab'){e.preventDefault();const a=e.target.selectionStart,b=e.target.selectionEnd;e.target.value=e.target.value.slice(0,a)+'    '+e.target.value.slice(b);e.target.selectionStart=e.target.selectionEnd=a+4;updateLineNumbers();}});
function modal(open=true){const box=$('#authModal');if(open){box.removeAttribute('hidden');box.style.removeProperty('display');document.body.style.overflow='hidden'}else{box.setAttribute('hidden','hidden');box.style.display='none';document.body.style.overflow=''}}
$('#profileBtn').onclick=()=>{
  if(state.user){
    if(confirm('Do you want to log out of your workspace?')){
      state.user = null;
      globalThis.localStorage?.removeItem('algolens_user');
      resetAnalysis();
      applyUser();
      toast('Logged out successfully.');
    }
  } else {
    modal(true);
  }
};
$('#closeModal').addEventListener('click',e=>{e.preventDefault();e.stopPropagation();setTimeout(()=>modal(false),60)});
$('#authModal').onclick=e=>{if(e.target.id==='authModal'||e.target.closest?.('#closeModal'))modal(false)};
document.addEventListener('click',e=>{if(e.target.closest?.('[data-close-auth]'))modal(false)});
document.addEventListener('keydown',e=>{if(e.key==='Escape')modal(false)});
$$('.auth-tabs button').forEach(b=>b.onclick=()=>{state.mode=b.dataset.mode;$$('.auth-tabs button').forEach(x=>x.classList.toggle('active',x===b));const reg=state.mode==='register';$('#nameField').hidden=!reg;$('#languageField').hidden=!reg;$('#authName').required=reg;$('.submit-auth').textContent=reg?'Create my workspace →':'Sign in →'});
$('#authForm').addEventListener('submit',async e=>{e.preventDefault();const reg=state.mode==='register',payload=reg?{name:$('#authName').value,email:$('#authEmail').value,password:$('#authPassword').value,language:$('input[name=lang]:checked').value}:{email:$('#authEmail').value,password:$('#authPassword').value};try{const r=await fetch(`/api/auth/${reg?'register':'login'}`,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(payload)});if(!r.ok)throw new Error((await r.json()).message||'Could not continue');state.user=await r.json();saveUser(state.user);applyUser();modal(false);toast(`Welcome${reg?' to AlgoLens':' back'}, ${state.user.name}.`)}catch(err){toast(err.message.includes('fetch')?'Start the Spring Boot app to create an account.':err.message)}});
function applyUser(){
  if(!state.user){
    $('#profileBtn span').textContent='SK';
    $('#languageLabel').textContent='JAVA';
    $('.editor-tabs button').textContent='Solution.java';
    $('#codeInput').value='';
    updateLineNumbers();
    loadHistory();
    return;
  }
  $('#profileBtn span').textContent=state.user.name.split(/\s+/).map(x=>x[0]).join('').slice(0,2).toUpperCase();
  $('#languageLabel').textContent=state.user.language;
  const ext={JAVA:'Solution.java',CPP:'solution.cpp',PYTHON:'solution.py'}[state.user.language];
  $('.editor-tabs button').textContent=ext;
  $('#codeInput').value=starterCodes[state.user.language];
  updateLineNumbers();
  loadHistory();
}
function resetAnalysis(){ $('#timeValue').textContent='—';$('#spaceValue').textContent='—';$('#timeLabel').textContent='Analyze code to calculate';$('#spaceLabel').textContent='Analyze code to calculate';$('#verdictText').textContent='Submit a solution to inspect its complexity and confidently detected patterns.';$('#signals').innerHTML='';renderPatterns([], '—');drawChart(); }
applyUser();resetAnalysis();loadHistory();updateLineNumbers();addEventListener('resize',()=>drawChart(chartKind($('#timeValue').textContent)));
