// Synthetic dataset generator for forum content recommendation (supervised binary classification).
// Output: forum_recommendation_dataset.csv  (balanced 50/50 on `engaged`, ~2000 rows, intentionally dirty)
//
// Run:  node generate_dataset.js

const fs = require('fs');
const path = require('path');

// ── Deterministic RNG so the dataset is reproducible ─────────────────────────
let _seed = 42;
function rand() {
  _seed = (_seed * 9301 + 49297) % 233280;
  return _seed / 233280;
}
const choice = (arr) => arr[Math.floor(rand() * arr.length)];
const randint = (a, b) => Math.floor(rand() * (b - a + 1)) + a;
const randfloat = (a, b) => a + rand() * (b - a);
const chance = (p) => rand() < p;

// ── Domain vocab matching the Spring Boot entities ───────────────────────────
const CATEGORIES = ['Grammar','TOEFL','IELTS','Speaking','Vocabulary','Writing','Listening','Reading','BusinessEnglish','Event'];
const LEVELS = ['Beginner','Intermediate','Advanced'];
const FRIEND_STATUSES = ['FRIEND','NOT_FRIEND','PENDING','BLOCKED'];

const TITLES_BY_CAT = {
  Grammar: ['#PastParticiple cheatsheet','Conditionals — when to use which','Phrasal verbs that confuse everyone','Articles a/an/the explained','Subjunctive mood basics'],
  TOEFL:   ['TOEFL Prep weekly thread','Integrated writing tips','Speaking task 1 templates','Reading section pacing','Score 100+ study plan'],
  IELTS:   ['IELTS band 7 roadmap','Cue card practice partners','Writing task 2 essay structures','Listening trap words','Speaking part 3 examples'],
  Speaking:['Daily speaking partners','Common pronunciation mistakes','Accent reduction drills','Small talk openers','Storytelling fluency'],
  Vocabulary:['Word of the day','Collocations you should know','Academic word list — week 4','Synonym swap challenge','Idioms in real conversations'],
  Writing: ['Essay feedback exchange','Cohesion vs coherence','Punctuation pitfalls','Formal vs informal register','Paraphrasing practice'],
  Listening:['Podcast recommendations','Note-taking techniques','Fast English drills','Accents around the world','Dictation practice thread'],
  Reading: ['Skimming vs scanning','Inference questions explained','Reading speed tracker','Article of the week','Vocabulary in context'],
  BusinessEnglish:['Email templates that work','Meeting phrases','Negotiation language','Job interview answers','Presentation openings'],
  Event:   ['Mock TOEFL Saturday','Speaking club Friday','Writing workshop signup','Mentor AMA Tuesday','Pronunciation bootcamp']
};

const FIRST = ['heni','sarra','amine','yassine','nour','rania','omar','salma','khalil','farah','adam','lina','iyed','wassim','mariem'];

// ── Dirtiness helpers ────────────────────────────────────────────────────────
// Returns its argument as-is most of the time, missing-encoded sometimes.
function maybeMissing(val, p = 0.05) {
  if (!chance(p)) return val;
  return choice(['', 'N/A', 'null', 'NaN', ' ']);
}
function dirtyCase(s) {
  const r = rand();
  if (r < 0.65) return s;
  if (r < 0.80) return s.toLowerCase();
  if (r < 0.92) return s.toUpperCase();
  return ' ' + s + ' '; // padded
}
function dirtyBool(b) {
  const r = rand();
  if (r < 0.5) return b ? 'true' : 'false';
  if (r < 0.75) return b ? 'yes' : 'no';
  if (r < 0.9)  return b ? '1' : '0';
  return b ? 'True' : 'False';
}
function dirtyDate(d) {
  // d = Date object
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth()+1).padStart(2,'0');
  const dd = String(d.getDate()).padStart(2,'0');
  const r = rand();
  if (r < 0.55) return `${yyyy}-${mm}-${dd} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}:00`; // ISO-ish
  if (r < 0.80) return `${mm}/${dd}/${yyyy}`;       // US
  if (r < 0.95) return `${dd}-${mm}-${yyyy}`;       // EU
  return `${yyyy}/${mm}/${dd}`;                     // mixed
}
function joinInterests(list) {
  // mix separators on purpose
  const r = rand();
  if (r < 0.6) return list.join(',');
  if (r < 0.85) return list.join('; ');
  return list.join('|');
}

// ── Build a pool of users and posts so features are coherent across rows ─────
const N_USERS = 220;
const N_POSTS = 600;

const users = Array.from({length: N_USERS}, (_, i) => {
  const interestCount = randint(1, 3);
  const interests = [];
  while (interests.length < interestCount) {
    const c = choice(CATEGORIES);
    if (!interests.includes(c)) interests.push(c);
  }
  return {
    user_id: 1000 + i,
    user_age: randint(16, 42),
    user_level: choice(LEVELS),
    user_learning_interests: interests,
    user_avg_session_minutes: +randfloat(2, 60).toFixed(1),
    user_posts_count: randint(0, 80),
    user_comments_made: randint(0, 250),
    user_likes_given: randint(0, 600),
    user_friends_count: randint(0, 120),
    days_since_signup: randint(1, 900),
    username: choice(FIRST) + randint(1, 999),
  };
});

const posts = Array.from({length: N_POSTS}, (_, i) => {
  const cat = choice(CATEGORIES);
  const title = choice(TITLES_BY_CAT[cat]);
  const author = users[randint(0, N_USERS - 1)];
  return {
    post_id: 5000 + i,
    post_topic_category: cat,
    post_topic_title: title,
    post_word_count: randint(15, 600),
    post_has_image: chance(0.35),
    post_likes: randint(0, 350),
    post_comments: randint(0, 90),
    post_reposts: randint(0, 25),
    post_age_hours: +randfloat(0.5, 24*30).toFixed(1),
    post_is_pinned: chance(0.08),
    author_user_id: author.user_id,
    created_at: new Date(Date.now() - Math.floor(rand() * 1000*60*60*24*120)),
  };
});

// ── Build the labelled examples — keep real signal in the data ───────────────
function probEngaged({ user, post, topicMatch, friendStatus, peerEng }) {
  // Logistic-ish hand-tuned scoring. Coefficients further sharpened so the
  // Bayes-optimal accuracy is around 0.85-0.88 (so cleaned classifiers can
  // reasonably reach 0.80+).
  let z = -3.0;
  z += 6.5 * topicMatch;                            // dominant signal
  z += friendStatus === 'FRIEND' ? 2.0 : friendStatus === 'BLOCKED' ? -3.0 : 0;
  z += Math.min(peerEng, 8) * 0.40;                 // social proof
  z += post.post_is_pinned ? 0.9 : 0;
  z += post.post_has_image ? 0.30 : 0;
  z += -0.004 * post.post_age_hours;                // recency
  z += 0.010 * Math.min(post.post_likes, 200);
  z += 0.005 * user.user_avg_session_minutes;
  z += user.user_friends_count > 30 ? 0.4 : 0;
  return 1 / (1 + Math.exp(-z));
}

const rowsByLabel = { 0: [], 1: [] };
const TARGET_PER_CLASS = 5000;

function makeRow() {
  const user = users[randint(0, N_USERS - 1)];
  const post = posts[randint(0, N_POSTS - 1)];

  // topic match: 1 if post category is in user's interests, with noise
  const inInterests = user.user_learning_interests.includes(post.post_topic_category);
  const topicMatchTrue = inInterests
    ? Math.min(1, randfloat(0.75, 0.99))
    : Math.max(0, randfloat(0.01, 0.25));

  const friendStatus = post.author_user_id === user.user_id
    ? 'NOT_FRIEND'
    : choice(FRIEND_STATUSES);

  const peerEng = randint(0, 12);

  const p = probEngaged({ user, post, topicMatch: topicMatchTrue, friendStatus, peerEng });
  const label = chance(p) ? 1 : 0;

  return {
    user_id: user.user_id,
    user_age: user.user_age,
    user_level: user.user_level,
    user_learning_interests: user.user_learning_interests.slice(),
    user_avg_session_minutes: user.user_avg_session_minutes,
    user_posts_count: user.user_posts_count,
    user_comments_made: user.user_comments_made,
    user_likes_given: user.user_likes_given,
    user_friends_count: user.user_friends_count,
    days_since_signup: user.days_since_signup,
    post_id: post.post_id,
    post_topic_category: post.post_topic_category,
    post_topic_title: post.post_topic_title,
    post_word_count: post.post_word_count,
    post_has_image: post.post_has_image,
    post_likes: post.post_likes,
    post_comments: post.post_comments,
    post_reposts: post.post_reposts,
    post_age_hours: post.post_age_hours,
    post_is_pinned: post.post_is_pinned,
    author_friend_status: friendStatus,
    topic_interest_match: +topicMatchTrue.toFixed(3),
    peer_engagement_count: peerEng,
    created_at: post.created_at,
    engaged: label,
  };
}

while (rowsByLabel[0].length < TARGET_PER_CLASS || rowsByLabel[1].length < TARGET_PER_CLASS) {
  const r = makeRow();
  if (rowsByLabel[r.engaged].length < TARGET_PER_CLASS) rowsByLabel[r.engaged].push(r);
}
let rows = [...rowsByLabel[0], ...rowsByLabel[1]];

// shuffle
for (let i = rows.length - 1; i > 0; i--) {
  const j = Math.floor(rand() * (i + 1));
  [rows[i], rows[j]] = [rows[j], rows[i]];
}

// ── Inject dirtiness ─────────────────────────────────────────────────────────
function dirtyRow(r) {
  const out = { ...r };

  // Categorical case/whitespace messiness
  out.user_level = dirtyCase(out.user_level);
  out.post_topic_category = dirtyCase(out.post_topic_category);

  // Mixed-format interests + sometimes missing
  out.user_learning_interests = chance(0.06)
    ? choice(['', 'N/A', 'null'])
    : joinInterests(out.user_learning_interests);

  // Booleans -> mixed encodings
  out.post_has_image = dirtyBool(out.post_has_image);
  out.post_is_pinned = dirtyBool(out.post_is_pinned);

  // Missing values scattered across columns
  out.user_age = maybeMissing(out.user_age, 0.07);
  out.user_avg_session_minutes = maybeMissing(out.user_avg_session_minutes, 0.05);
  out.topic_interest_match = maybeMissing(out.topic_interest_match, 0.08);
  out.user_friends_count = maybeMissing(out.user_friends_count, 0.03);
  out.post_word_count = maybeMissing(out.post_word_count, 0.04);

  // Numeric outliers / impossible values (rare)
  if (chance(0.01) && out.user_age !== '') out.user_age = 999;
  if (chance(0.01)) out.post_word_count = -randint(5, 80);
  if (chance(0.005)) out.user_avg_session_minutes = 9999;

  // Dates in mixed formats
  out.created_at = dirtyDate(out.created_at);

  // Stray whitespace on title sometimes
  if (chance(0.05)) out.post_topic_title = '  ' + out.post_topic_title;

  return out;
}
rows = rows.map(dirtyRow);

// ── Inject ~30 exact duplicate rows (a classic "needs cleaning" issue) ───────
for (let i = 0; i < 30; i++) {
  rows.push({ ...rows[randint(0, rows.length - 1)] });
}

// final shuffle
for (let i = rows.length - 1; i > 0; i--) {
  const j = Math.floor(rand() * (i + 1));
  [rows[i], rows[j]] = [rows[j], rows[i]];
}

// ── Write CSV ────────────────────────────────────────────────────────────────
const COLS = [
  'user_id','user_age','user_level','user_learning_interests','user_avg_session_minutes',
  'user_posts_count','user_comments_made','user_likes_given','user_friends_count','days_since_signup',
  'post_id','post_topic_category','post_topic_title','post_word_count','post_has_image',
  'post_likes','post_comments','post_reposts','post_age_hours','post_is_pinned',
  'author_friend_status','topic_interest_match','peer_engagement_count','created_at','engaged',
];

function csvCell(v) {
  if (v === null || v === undefined) return '';
  const s = String(v);
  if (/[",\n]/.test(s)) return '"' + s.replace(/"/g, '""') + '"';
  return s;
}

const lines = [COLS.join(',')];
for (const r of rows) lines.push(COLS.map(c => csvCell(r[c])).join(','));

const outPath = path.join(__dirname, 'forum_recommendation_dataset.csv');
fs.writeFileSync(outPath, lines.join('\n'), 'utf8');

const counts = rows.reduce((a, r) => (a[r.engaged] = (a[r.engaged]||0)+1, a), {});
console.log(`Wrote ${rows.length} rows -> ${outPath}`);
console.log(`Class balance: engaged=1 -> ${counts[1]}, engaged=0 -> ${counts[0]}`);
console.log(`(Includes ~30 duplicate rows on purpose.)`);
