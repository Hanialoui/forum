// Builds train.ipynb — a clean, presentation-ready notebook for the forum
// content recommendation supervised model (LR + Random Forest).
// Run:  node build_notebook.js

const fs = require('fs');
const path = require('path');

const md  = (...lines) => ({ cell_type: 'markdown', metadata: {}, source: linify(lines) });
const py  = (...lines) => ({ cell_type: 'code', metadata: {}, execution_count: null, outputs: [], source: linify(lines) });

function linify(lines) {
  const flat = lines.flat();
  return flat.map((l, i) => i === flat.length - 1 ? l : l + '\n');
}

const cells = [];

cells.push(md(
  '# Forum Content Recommendation — Supervised ML',
  '',
  '**Business goal:** increase student engagement and knowledge sharing through meaningful discussions.',
  '',
  '**Data science goal (binary classification):** predict whether a given (student, forum post) pair will lead to engagement (`engaged = 1`) or not (`engaged = 0`).',
  '',
  '**Models compared (linear / bagging / boosting / neural):**',
  '1. **Logistic Regression** — linear baseline, fully interpretable.',
  '2. **Random Forest** — bagging ensemble, robust to outliers, gives feature importance.',
  '3. **XGBoost** — gradient boosting, industry standard for tabular data.',
  '4. **Artificial Neural Network (ANN)** — Multi-Layer Perceptron built with Keras. Uses Dense layers, ReLU activation, sigmoid output, Binary Cross-Entropy loss, and the Adam optimizer (adaptive learning rate). Trained with forward propagation + backpropagation (chain rule).',
  '',
  '**Notebook plan:**',
  '1. Imports & load data',
  '2. Quick look (shape, dtypes, summary)',
  '3. **Cleaning** — duplicates, missing values, casing, booleans, dates, outliers, imputation',
  '4. EDA — class balance & correlations',
  '5. Feature engineering — multi-hot interests, one-hot categoricals, datetime parts',
  '6. Train / test split',
  '7. Model 1 — Logistic Regression',
  '8. Model 2 — Random Forest',
  '9. Model 3 — XGBoost',
  '10. Model 4 — Artificial Neural Network (Keras / TensorFlow)',
  '11. Side-by-side comparison',
  '12. Cross-validation',
  '13. Save best model'
));

cells.push(md('## 0. Install (only run once)'));
cells.push(py(
  '# Uncomment if running in a fresh environment',
  '# !pip install pandas numpy scikit-learn matplotlib seaborn joblib xgboost tensorflow'
));

cells.push(md('## 1. Imports'));
cells.push(py(
  'import re',
  'import warnings',
  'import joblib',
  'import numpy as np',
  'import pandas as pd',
  'import matplotlib.pyplot as plt',
  'import seaborn as sns',
  '',
  'from sklearn.model_selection import train_test_split, cross_val_score, StratifiedKFold',
  'from sklearn.preprocessing import StandardScaler',
  'from sklearn.linear_model import LogisticRegression',
  'from sklearn.ensemble import RandomForestClassifier',
  'from xgboost import XGBClassifier',
  '',
  '# ANN (course chapter 6)',
  'import tensorflow as tf',
  'from tensorflow.keras.models import Sequential',
  'from tensorflow.keras.layers import Dense, Dropout, Input',
  'from tensorflow.keras.optimizers import Adam',
  'from tensorflow.keras.callbacks import EarlyStopping',
  'from sklearn.metrics import (',
  '    accuracy_score, precision_score, recall_score, f1_score, roc_auc_score,',
  '    classification_report, confusion_matrix, roc_curve, ConfusionMatrixDisplay,',
  ')',
  '',
  'warnings.filterwarnings("ignore")',
  'sns.set_style("whitegrid")',
  'pd.set_option("display.max_columns", None)',
  'RANDOM_STATE = 42'
));

cells.push(md(
  '## 2. Load the dataset',
  '',
  'Works in **local Jupyter** *and* **Google Colab**:',
  '- Local: place `forum_recommendation_dataset.csv` next to this notebook.',
  '- Colab: the cell will prompt you to upload the CSV the first time.'
));
cells.push(py(
  'import os',
  'CSV_NAME = "forum_recommendation_dataset.csv"',
  '',
  'def load_dataset():',
  '    if os.path.exists(CSV_NAME):',
  '        return pd.read_csv(CSV_NAME)',
  '    try:',
  '        from google.colab import files  # type: ignore',
  '        print(f"{CSV_NAME} not found. Please upload it now...")',
  '        uploaded = files.upload()',
  '        if CSV_NAME not in uploaded:',
  '            # accept any uploaded csv as a fallback',
  '            for name in uploaded:',
  '                if name.lower().endswith(".csv"):',
  '                    return pd.read_csv(name)',
  '            raise FileNotFoundError("No CSV uploaded.")',
  '        return pd.read_csv(CSV_NAME)',
  '    except ImportError:',
  '        raise FileNotFoundError(',
  '            f"{CSV_NAME} not found in working directory: {os.getcwd()}. "',
  '            "Place the CSV next to this notebook."',
  '        )',
  '',
  'df = load_dataset()',
  'print("Shape:", df.shape)',
  'df.head()'
));

cells.push(py(
  'df.info()'
));

cells.push(py(
  'df.describe(include="all").T'
));

cells.push(md(
  '## 3. Cleaning',
  '',
  'The dataset is intentionally dirty. We handle each issue explicitly.'
));

cells.push(md('### 3.1 Drop exact duplicates'));
cells.push(py(
  'print("Duplicates before:", df.duplicated().sum())',
  'df = df.drop_duplicates().reset_index(drop=True)',
  'print("Shape after dedup:", df.shape)'
));

cells.push(md('### 3.2 Normalize missing-value tokens',
  '',
  'Missing values appear as `""`, `" "`, `N/A`, `null`, `NaN`. Convert all to real `NaN`.'));
cells.push(py(
  'MISSING_TOKENS = ["", " ", "N/A", "n/a", "na", "null", "NULL", "NaN", "nan", "None"]',
  '',
  '# Strip whitespace on all object columns first',
  'for c in df.select_dtypes(include="object").columns:',
  '    df[c] = df[c].astype(str).str.strip()',
  '',
  'df = df.replace(MISSING_TOKENS, np.nan)',
  'df.isna().sum().sort_values(ascending=False)'
));

cells.push(md('### 3.3 Normalize categorical case & whitespace'));
cells.push(py(
  'df["user_level"] = df["user_level"].str.strip().str.title()',
  'df["post_topic_category"] = df["post_topic_category"].str.strip().str.lower()',
  'df["author_friend_status"] = df["author_friend_status"].str.strip().str.upper()',
  '',
  'print(df["user_level"].value_counts(dropna=False))',
  'print()',
  'print(df["post_topic_category"].value_counts(dropna=False))'
));

cells.push(md('### 3.4 Unify boolean encodings',
  '',
  '`post_has_image` and `post_is_pinned` arrive as `true/false`, `yes/no`, `1/0`, `True/False`.'));
cells.push(py(
  'TRUE_TOKENS  = {"true", "1", "yes", "y", "t"}',
  'FALSE_TOKENS = {"false", "0", "no", "n", "f"}',
  '',
  'def to_bool(v):',
  '    if pd.isna(v):',
  '        return np.nan',
  '    s = str(v).strip().lower()',
  '    if s in TRUE_TOKENS:  return 1',
  '    if s in FALSE_TOKENS: return 0',
  '    return np.nan',
  '',
  'for col in ["post_has_image", "post_is_pinned"]:',
  '    df[col] = df[col].apply(to_bool)',
  '',
  'df[["post_has_image", "post_is_pinned"]].value_counts(dropna=False)'
));

cells.push(md('### 3.5 Coerce numeric columns'));
cells.push(py(
  'numeric_cols = [',
  '    "user_age", "user_avg_session_minutes", "user_posts_count", "user_comments_made",',
  '    "user_likes_given", "user_friends_count", "days_since_signup", "post_word_count",',
  '    "post_likes", "post_comments", "post_reposts", "post_age_hours",',
  '    "topic_interest_match", "peer_engagement_count",',
  ']',
  'for c in numeric_cols:',
  '    df[c] = pd.to_numeric(df[c], errors="coerce")',
  '',
  'df[numeric_cols].describe().T'
));

cells.push(md('### 3.6 Parse multi-format dates',
  '',
  '`created_at` is a mix of ISO, US (`MM/DD/YYYY`), EU (`DD-MM-YYYY`) and `YYYY/MM/DD`.'));
cells.push(py(
  'def parse_date(s):',
  '    if pd.isna(s):',
  '        return pd.NaT',
  '    s = str(s).strip()',
  '    for fmt in ("%Y-%m-%d %H:%M:%S", "%Y-%m-%d", "%m/%d/%Y", "%d-%m-%Y", "%Y/%m/%d"):',
  '        try:',
  '            return pd.to_datetime(s, format=fmt)',
  '        except (ValueError, TypeError):',
  '            continue',
  '    return pd.to_datetime(s, errors="coerce")',
  '',
  'df["created_at"] = df["created_at"].apply(parse_date)',
  'print("Unparsed dates:", df["created_at"].isna().sum())',
  'df["created_at"].head()'
));

cells.push(md('### 3.7 Outlier handling',
  '',
  'Examples we observed: `user_age = 999`, negative `post_word_count`, `user_avg_session_minutes = 9999`.'));
cells.push(py(
  '# Plausible bounds',
  'df.loc[(df["user_age"] < 13) | (df["user_age"] > 80), "user_age"] = np.nan',
  'df.loc[df["user_avg_session_minutes"] > 240, "user_avg_session_minutes"] = 240  # cap at 4h',
  'df.loc[df["post_word_count"] < 0, "post_word_count"] = np.nan',
  '',
  'df[["user_age", "user_avg_session_minutes", "post_word_count"]].describe().T'
));

cells.push(md('### 3.8 Imputation',
  '',
  '**Important:** for high-signal features we *also* keep a `was_missing` flag column so the model knows when the value was originally unknown (median imputation alone destroys that signal — that was our biggest accuracy leak).'));
cells.push(py(
  '# 1) Flag missingness on the high-signal features BEFORE imputing',
  'flag_cols = ["topic_interest_match", "post_word_count", "user_avg_session_minutes", "user_age"]',
  'for c in flag_cols:',
  '    df[f"{c}_was_missing"] = df[c].isna().astype(int)',
  '',
  '# 2) Numeric → median (robust to outliers)',
  'for c in numeric_cols:',
  '    df[c] = df[c].fillna(df[c].median())',
  '',
  '# Booleans → mode',
  'for c in ["post_has_image", "post_is_pinned"]:',
  '    df[c] = df[c].fillna(df[c].mode()[0]).astype(int)',
  '',
  '# Categoricals → mode (or "unknown" sentinel)',
  'df["user_level"] = df["user_level"].fillna(df["user_level"].mode()[0])',
  'df["post_topic_category"] = df["post_topic_category"].fillna(df["post_topic_category"].mode()[0])',
  'df["author_friend_status"] = df["author_friend_status"].fillna("NOT_FRIEND")',
  'df["user_learning_interests"] = df["user_learning_interests"].fillna("unknown")',
  '',
  'print("Total NaNs left:", df.isna().sum().sum())'
));

cells.push(md('## 4. EDA'));

cells.push(md('### 4.1 Class balance'));
cells.push(py(
  'print(df["engaged"].value_counts())',
  'plt.figure(figsize=(5, 3))',
  'sns.countplot(data=df, x="engaged", palette="Set2")',
  'plt.title("Class balance (target = engaged)")',
  'plt.show()'
));

cells.push(md('### 4.2 Correlation with the target'));
cells.push(py(
  'plt.figure(figsize=(6, 8))',
  'corr = df[numeric_cols + ["engaged"]].corr()[["engaged"]].sort_values("engaged", ascending=False)',
  'sns.heatmap(corr, annot=True, cmap="RdBu_r", center=0, fmt=".2f")',
  'plt.title("Correlation with engaged")',
  'plt.show()'
));

cells.push(md('## 5. Feature engineering'));

cells.push(md('### 5.1 Multi-hot encode `user_learning_interests`',
  '',
  'Stored as a delimited string with mixed separators (`,`, `;`, `|`). One column per interest category.'));
cells.push(py(
  'def split_interests(s):',
  '    if s == "unknown":',
  '        return []',
  '    return [x.strip().lower() for x in re.split(r"[,;|]", str(s)) if x.strip()]',
  '',
  'df["interests_list"] = df["user_learning_interests"].apply(split_interests)',
  'all_interests = sorted({i for lst in df["interests_list"] for i in lst})',
  'for cat in all_interests:',
  '    df[f"int_{cat}"] = df["interests_list"].apply(lambda l: int(cat in l))',
  '',
  'print("Interest features:", [f"int_{c}" for c in all_interests])'
));

cells.push(md('### 5.2 Datetime parts from `created_at`'));
cells.push(py(
  'df["post_hour"] = df["created_at"].dt.hour.fillna(12).astype(int)',
  'df["post_dayofweek"] = df["created_at"].dt.dayofweek.fillna(0).astype(int)'
));

cells.push(md('### 5.3 One-hot encode the remaining categoricals & drop unused columns'));
cells.push(py(
  'df_encoded = pd.get_dummies(',
  '    df,',
  '    columns=["user_level", "post_topic_category", "author_friend_status"],',
  '    drop_first=False,',
  ')',
  '',
  'drop_cols = ["user_id", "post_id", "post_topic_title",',
  '             "user_learning_interests", "interests_list", "created_at"]',
  'df_encoded = df_encoded.drop(columns=drop_cols)',
  '',
  'print("Shape after one-hot:", df_encoded.shape)'
));

cells.push(md('### 5.4 Interaction features',
  '',
  'Real engagement isn\\u2019t additive: a friend-authored post **about a topic the user cares about** is dramatically more engaging than either signal alone. Linear models can\\u2019t learn this from raw columns, so we hand-craft the interactions.'));
cells.push(py(
  '# friend-author and topic match together',
  'if "author_friend_status_FRIEND" in df_encoded.columns:',
  '    df_encoded["friend_x_match"] = (',
  '        df_encoded["author_friend_status_FRIEND"] * df_encoded["topic_interest_match"]',
  '    )',
  '',
  '# social proof scaled by personal interest',
  'df_encoded["match_x_peer"] = (',
  '    df_encoded["topic_interest_match"] * df_encoded["peer_engagement_count"]',
  ')',
  '',
  '# recency-weighted likes (a popular OLD post matters less)',
  'df_encoded["likes_per_hour"] = df_encoded["post_likes"] / (df_encoded["post_age_hours"] + 1)',
  '',
  'print("Final shape:", df_encoded.shape)',
  'df_encoded.head()'
));

cells.push(md('## 6. Train / test split (stratified)'));
cells.push(py(
  'X = df_encoded.drop(columns=["engaged"])',
  'y = df_encoded["engaged"].astype(int)',
  '',
  'X_train, X_test, y_train, y_test = train_test_split(',
  '    X, y, test_size=0.2, stratify=y, random_state=RANDOM_STATE',
  ')',
  'print("Train:", X_train.shape, "  Test:", X_test.shape)'
));

cells.push(md('## 7. Model 1 — Logistic Regression',
  '',
  'Linear models need feature scaling so coefficients are comparable.'));
cells.push(py(
  'scaler = StandardScaler()',
  'X_train_scaled = scaler.fit_transform(X_train)',
  'X_test_scaled  = scaler.transform(X_test)',
  '',
  'lr = LogisticRegression(max_iter=2000, random_state=RANDOM_STATE)',
  'lr.fit(X_train_scaled, y_train)',
  '',
  'y_pred_lr  = lr.predict(X_test_scaled)',
  'y_proba_lr = lr.predict_proba(X_test_scaled)[:, 1]',
  '',
  'print(classification_report(y_test, y_pred_lr, target_names=["Not engaged", "Engaged"]))',
  'print("ROC-AUC:", round(roc_auc_score(y_test, y_proba_lr), 4))'
));

cells.push(py(
  'ConfusionMatrixDisplay(',
  '    confusion_matrix(y_test, y_pred_lr),',
  '    display_labels=["Not engaged", "Engaged"],',
  ').plot(cmap="Blues")',
  'plt.title("Logistic Regression — Confusion Matrix")',
  'plt.show()'
));

cells.push(py(
  'coef_df = (pd.DataFrame({"feature": X.columns, "coef": lr.coef_[0]})',
  '             .reindex(pd.DataFrame({"feature": X.columns, "coef": lr.coef_[0]}).coef.abs().sort_values(ascending=False).index)',
  '             .head(15))',
  '',
  'plt.figure(figsize=(8, 6))',
  'sns.barplot(data=coef_df, x="coef", y="feature", palette="vlag")',
  'plt.title("Logistic Regression — top 15 coefficients (by |value|)")',
  'plt.show()'
));

cells.push(md('## 8. Model 2 — Random Forest',
  '',
  'Tree ensembles do not need scaling and are robust to leftover noise. Hyperparameters tuned for this dataset:',
  '- `n_estimators=500` — more trees → more stable predictions.',
  '- `max_depth=12` — caps tree depth to prevent overfitting on duplicates / outliers.',
  '- `min_samples_leaf=5` — each leaf must cover at least 5 rows (kills noisy splits).',
  '- `max_features="sqrt"` — each split considers a random subset of features (decorrelates trees).'));
cells.push(py(
  'rf = RandomForestClassifier(',
  '    n_estimators=500,',
  '    max_depth=12,',
  '    min_samples_leaf=5,',
  '    max_features="sqrt",',
  '    n_jobs=-1,',
  '    random_state=RANDOM_STATE,',
  ')',
  'rf.fit(X_train, y_train)',
  '',
  'y_pred_rf  = rf.predict(X_test)',
  'y_proba_rf = rf.predict_proba(X_test)[:, 1]',
  '',
  'print(classification_report(y_test, y_pred_rf, target_names=["Not engaged", "Engaged"]))',
  'print("ROC-AUC:", round(roc_auc_score(y_test, y_proba_rf), 4))'
));

cells.push(py(
  'ConfusionMatrixDisplay(',
  '    confusion_matrix(y_test, y_pred_rf),',
  '    display_labels=["Not engaged", "Engaged"],',
  ').plot(cmap="Greens")',
  'plt.title("Random Forest — Confusion Matrix")',
  'plt.show()'
));

cells.push(py(
  'imp = (pd.DataFrame({"feature": X.columns, "importance": rf.feature_importances_})',
  '         .sort_values("importance", ascending=False)',
  '         .head(15))',
  '',
  'plt.figure(figsize=(8, 6))',
  'sns.barplot(data=imp, x="importance", y="feature", palette="viridis")',
  'plt.title("Random Forest — top 15 feature importances")',
  'plt.show()'
));

cells.push(md('## 9. Model 3 — XGBoost (Gradient Boosting)',
  '',
  'XGBoost builds trees **sequentially**: each new tree fixes the errors of the previous ones. This is different from Random Forest which builds independent trees in parallel. For tabular data it usually wins.',
  '',
  '- `n_estimators=400` — number of boosting rounds.',
  '- `max_depth=6` — shallower trees are typical for boosting (each tree is a weak learner).',
  '- `learning_rate=0.05` — small steps; combined with more rounds gives smoother convergence.',
  '- `subsample=0.8`, `colsample_bytree=0.8` — row/column subsampling fights overfitting.'));
cells.push(py(
  'xgb = XGBClassifier(',
  '    n_estimators=400,',
  '    max_depth=6,',
  '    learning_rate=0.05,',
  '    subsample=0.8,',
  '    colsample_bytree=0.8,',
  '    eval_metric="logloss",',
  '    n_jobs=-1,',
  '    random_state=RANDOM_STATE,',
  ')',
  'xgb.fit(X_train, y_train)',
  '',
  'y_pred_xgb  = xgb.predict(X_test)',
  'y_proba_xgb = xgb.predict_proba(X_test)[:, 1]',
  '',
  'print(classification_report(y_test, y_pred_xgb, target_names=["Not engaged", "Engaged"]))',
  'print("ROC-AUC:", round(roc_auc_score(y_test, y_proba_xgb), 4))'
));

cells.push(py(
  'ConfusionMatrixDisplay(',
  '    confusion_matrix(y_test, y_pred_xgb),',
  '    display_labels=["Not engaged", "Engaged"],',
  ').plot(cmap="Oranges")',
  'plt.title("XGBoost — Confusion Matrix")',
  'plt.show()'
));

cells.push(py(
  'imp_xgb = (pd.DataFrame({"feature": X.columns, "importance": xgb.feature_importances_})',
  '             .sort_values("importance", ascending=False)',
  '             .head(15))',
  '',
  'plt.figure(figsize=(8, 6))',
  'sns.barplot(data=imp_xgb, x="importance", y="feature", palette="rocket")',
  'plt.title("XGBoost — top 15 feature importances")',
  'plt.show()'
));

cells.push(md('## 10. Model 4 — Artificial Neural Network (ANN)',
  '',
  '### Principe (rappel du chapitre 6 — Les réseaux de neurones)',
  '',
  'Un **réseau de neurones** est un empilement de couches de neurones (perceptrons) qui apprennent ensemble.',
  '',
  '**Le perceptron — Forward Propagation**',
  '$$\\hat{y} = g\\!\\left(w_0 + \\sum_{i=1}^{m} x_i \\, w_i\\right)$$',
  '- **Inputs** $x_1, \\dots, x_m$ — nos features (ici ~30 colonnes après l\\u2019encodage).',
  '- **Weights** $w_i$ — appris par le réseau ; $w_0$ est le **biais**.',
  '- **Sum** $z = w_0 + X^T W$ — combinaison linéaire des entrées.',
  '- **Non-linearity** $g(z)$ — fonction d\\u2019activation. Sans elle, peu importe le nombre de couches, le modèle reste linéaire (cf. slide *Importance of Activation Functions*).',
  '- **Output** $\\hat{y}$.',
  '',
  '**Architecture utilisée — Deep Neural Network (multi-couches Dense)**',
  '- **Input layer** — un neurone par feature.',
  '- **Hidden layer 1** — 64 neurones, activation **ReLU** ($g(z) = \\max(0, z)$).',
  '- **Hidden layer 2** — 32 neurones, activation **ReLU**.',
  '- **Output layer** — 1 neurone, activation **sigmoïde** (sortie = probabilité d\\u2019engagement entre 0 et 1).',
  '',
  'Comme tous les inputs sont connectés à tous les neurones de la couche suivante, ce sont des **couches Dense** (densément connectées).',
  '',
  '### Apprentissage — Loss + Gradient Descent + Backpropagation',
  '',
  '1. **Quantifying Loss — Binary Cross-Entropy** (problème de classification binaire) :',
  '$$J(W) = -\\frac{1}{n} \\sum_{i=1}^{n} \\Big[ y^{(i)} \\log f(x^{(i)};W) + (1-y^{(i)}) \\log (1 - f(x^{(i)};W)) \\Big]$$',
  '',
  '2. **Gradient Descent — algorithme du chapitre :**',
  '   1. Initialiser les poids aléatoirement $W \\sim \\mathcal{N}(0, \\sigma^2)$.',
  '   2. Calculer le gradient $\\partial J(W) / \\partial W$ par **backpropagation** (règle de la chaîne).',
  '   3. Mettre à jour : $W \\leftarrow W - \\eta \\, \\partial J(W)/\\partial W$.',
  '   4. Répéter jusqu\\u2019à convergence.',
  '',
  '3. **Adaptive Learning Rate — Adam :** au lieu d\\u2019un $\\eta$ fixe (qui converge mal — cf. slides *Setting the Learning Rate*), Adam adapte $\\eta$ par poids selon la taille du gradient. C\\u2019est l\\u2019optimiseur recommandé par le cours.',
  '',
  '4. **Dropout & EarlyStopping** — régularisations : Dropout désactive aléatoirement des neurones pour éviter le sur-apprentissage ; EarlyStopping arrête l\\u2019entraînement quand la perte de validation cesse de baisser.',
  '',
  '> Comme la régression logistique, un ANN exige des features **standardisées** : on réutilise `X_train_scaled` / `X_test_scaled`.'
));

cells.push(py(
  '# Reproductibilité',
  'tf.random.set_seed(RANDOM_STATE)',
  'np.random.seed(RANDOM_STATE)',
  '',
  'n_features = X_train_scaled.shape[1]',
  '',
  '# Construction du Multi-Layer Perceptron (Sequential = empilement de couches)',
  'ann = Sequential([',
  '    Input(shape=(n_features,)),                # Couche d\'entrée',
  '    Dense(64, activation="relu"),               # Hidden layer 1 (ReLU)',
  '    Dropout(0.3),                                # Régularisation',
  '    Dense(32, activation="relu"),               # Hidden layer 2 (ReLU)',
  '    Dropout(0.2),',
  '    Dense(1,  activation="sigmoid"),             # Sortie binaire (probabilité)',
  '])',
  '',
  '# Compilation : loss = Binary Cross-Entropy, optimiseur = Adam (adaptive LR)',
  'ann.compile(',
  '    optimizer=Adam(learning_rate=1e-3),',
  '    loss="binary_crossentropy",',
  '    metrics=["accuracy"],',
  ')',
  'ann.summary()'
));

cells.push(py(
  '# Entraînement par gradient descent + backpropagation, sur plusieurs epochs',
  'es = EarlyStopping(monitor="val_loss", patience=8, restore_best_weights=True)',
  '',
  'history = ann.fit(',
  '    X_train_scaled, y_train,',
  '    validation_split=0.15,',
  '    epochs=80,',
  '    batch_size=64,',
  '    callbacks=[es],',
  '    verbose=0,',
  ')',
  'print(f"Entraînement arrêté après {len(history.history[\'loss\'])} epoch(s)")'
));

cells.push(py(
  '# Courbes d\'apprentissage : la loss doit baisser, l\'accuracy doit monter',
  'fig, ax = plt.subplots(1, 2, figsize=(12, 4))',
  'ax[0].plot(history.history["loss"], label="train")',
  'ax[0].plot(history.history["val_loss"], label="val")',
  'ax[0].set_title("Loss (Binary Cross-Entropy)")',
  'ax[0].set_xlabel("Epoch"); ax[0].legend()',
  '',
  'ax[1].plot(history.history["accuracy"], label="train")',
  'ax[1].plot(history.history["val_accuracy"], label="val")',
  'ax[1].set_title("Accuracy")',
  'ax[1].set_xlabel("Epoch"); ax[1].legend()',
  '',
  'plt.suptitle("Apprentissage du réseau de neurones")',
  'plt.tight_layout(); plt.show()'
));

cells.push(py(
  '# Évaluation sur le jeu de test',
  'y_proba_ann = ann.predict(X_test_scaled, verbose=0).ravel()',
  'y_pred_ann  = (y_proba_ann >= 0.5).astype(int)',
  '',
  'print(classification_report(y_test, y_pred_ann, target_names=["Not engaged", "Engaged"]))',
  'print("ROC-AUC:", round(roc_auc_score(y_test, y_proba_ann), 4))'
));

cells.push(py(
  'ConfusionMatrixDisplay(',
  '    confusion_matrix(y_test, y_pred_ann),',
  '    display_labels=["Not engaged", "Engaged"],',
  ').plot(cmap="Purples")',
  'plt.title("ANN — Confusion Matrix")',
  'plt.show()'
));

cells.push(md('## 11. Side-by-side comparison'));
cells.push(py(
  'def metrics(y_true, y_pred, y_proba):',
  '    return {',
  '        "Accuracy":  accuracy_score(y_true, y_pred),',
  '        "Precision": precision_score(y_true, y_pred),',
  '        "Recall":    recall_score(y_true, y_pred),',
  '        "F1":        f1_score(y_true, y_pred),',
  '        "ROC-AUC":   roc_auc_score(y_true, y_proba),',
  '    }',
  '',
  'results = pd.DataFrame({',
  '    "Logistic Regression": metrics(y_test, y_pred_lr,  y_proba_lr),',
  '    "Random Forest":       metrics(y_test, y_pred_rf,  y_proba_rf),',
  '    "XGBoost":             metrics(y_test, y_pred_xgb, y_proba_xgb),',
  '    "ANN (Keras)":         metrics(y_test, y_pred_ann, y_proba_ann),',
  '})',
  'results.round(4)'
));

cells.push(py(
  'fpr_lr,  tpr_lr,  _ = roc_curve(y_test, y_proba_lr)',
  'fpr_rf,  tpr_rf,  _ = roc_curve(y_test, y_proba_rf)',
  'fpr_xgb, tpr_xgb, _ = roc_curve(y_test, y_proba_xgb)',
  'fpr_ann, tpr_ann, _ = roc_curve(y_test, y_proba_ann)',
  '',
  'plt.figure(figsize=(7, 6))',
  'plt.plot(fpr_lr,  tpr_lr,  label=f"Logistic Regression  (AUC = {roc_auc_score(y_test, y_proba_lr):.3f})")',
  'plt.plot(fpr_rf,  tpr_rf,  label=f"Random Forest        (AUC = {roc_auc_score(y_test, y_proba_rf):.3f})")',
  'plt.plot(fpr_xgb, tpr_xgb, label=f"XGBoost              (AUC = {roc_auc_score(y_test, y_proba_xgb):.3f})")',
  'plt.plot(fpr_ann, tpr_ann, label=f"ANN                  (AUC = {roc_auc_score(y_test, y_proba_ann):.3f})")',
  'plt.plot([0, 1], [0, 1], "--", color="gray")',
  'plt.xlabel("False Positive Rate"); plt.ylabel("True Positive Rate")',
  'plt.title("ROC curves")',
  'plt.legend()',
  'plt.show()'
));

cells.push(md('## 12. Cross-validation (stratified 5-fold ROC-AUC)',
  '',
  '> L\\u2019ANN n\\u2019est pas inclus dans cette boucle k-fold : chaque pli demanderait un entraînement complet du réseau (long). On conserve son score sur le jeu de test mis de côté en §10. Les trois autres modèles sont peu coûteux à ré-entraîner.'));
cells.push(py(
  'cv = StratifiedKFold(n_splits=5, shuffle=True, random_state=RANDOM_STATE)',
  '',
  'X_scaled_full = StandardScaler().fit_transform(X)',
  'cv_lr  = cross_val_score(lr,  X_scaled_full, y, cv=cv, scoring="roc_auc", n_jobs=-1)',
  'cv_rf  = cross_val_score(rf,  X,             y, cv=cv, scoring="roc_auc", n_jobs=-1)',
  'cv_xgb = cross_val_score(xgb, X,             y, cv=cv, scoring="roc_auc", n_jobs=-1)',
  '',
  'print(f"Logistic Regression  CV ROC-AUC: {cv_lr.mean():.4f}  +/- {cv_lr.std():.4f}")',
  'print(f"Random Forest        CV ROC-AUC: {cv_rf.mean():.4f}  +/- {cv_rf.std():.4f}")',
  'print(f"XGBoost              CV ROC-AUC: {cv_xgb.mean():.4f} +/- {cv_xgb.std():.4f}")',
  'print(f"ANN (hold-out only)     ROC-AUC: {roc_auc_score(y_test, y_proba_ann):.4f}")'
));

cells.push(md('## 13. Save the best model',
  '',
  'Les modèles sklearn / XGBoost sont sérialisés avec **joblib**. Les modèles Keras se sauvegardent dans le format natif `.keras`.'));
cells.push(py(
  '# Comparaison : moyenne CV pour les 3 modèles sklearn, hold-out pour l\'ANN',
  'scores = {',
  '    "logistic_regression": cv_lr.mean(),',
  '    "random_forest":       cv_rf.mean(),',
  '    "xgboost":             cv_xgb.mean(),',
  '    "ann":                 roc_auc_score(y_test, y_proba_ann),',
  '}',
  'best_name = max(scores, key=scores.get)',
  'print(f"Meilleur modèle : {best_name}  (ROC-AUC = {scores[best_name]:.4f})")',
  '',
  'if best_name == "ann":',
  '    ann.save("best_model_ann.keras")',
  '    joblib.dump({"features": list(X.columns), "scaler": scaler},',
  '                "best_model_ann_meta.joblib")',
  '    print("Sauvé : best_model_ann.keras  +  best_model_ann_meta.joblib")',
  'else:',
  '    model_map = {"logistic_regression": lr, "random_forest": rf, "xgboost": xgb}',
  '    bundle = {',
  '        "model": model_map[best_name],',
  '        "features": list(X.columns),',
  '        "scaler": scaler if best_name == "logistic_regression" else None,',
  '    }',
  '    out = f"best_model_{best_name}.joblib"',
  '    joblib.dump(bundle, out)',
  '    print("Sauvé :", out)'
));

cells.push(md(
  '## 14. Conclusions (à remplir après exécution)',
  '',
  '- Quel modèle gagne sur le ROC-AUC (CV pour sklearn / XGBoost, hold-out pour l\\u2019ANN) ?',
  '- **ANN vs XGBoost** : sur ces données tabulaires de taille modeste (~10k lignes), XGBoost gagne souvent. L\\u2019ANN brille surtout sur les très gros volumes ou les données non structurées (image, texte). Que constatez-vous ?',
  '- Top features (importances XGBoost / Random Forest et coefficients LR).',
  '- Interprétation business : comment transformer ces features en règle de recommandation ?'
));

const notebook = {
  cells,
  metadata: {
    kernelspec: { display_name: 'Python 3', language: 'python', name: 'python3' },
    language_info: { name: 'python', version: '3.11' },
  },
  nbformat: 4,
  nbformat_minor: 5,
};

const out = path.join(__dirname, 'train.ipynb');
fs.writeFileSync(out, JSON.stringify(notebook, null, 1), 'utf8');
console.log('Wrote', out, '(' + cells.length + ' cells)');
