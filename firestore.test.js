const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");
const serverTimestamp = () => new Date(Date.now() - 1000);

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function validTransaction(uid) {
  return {
    userId: uid,
    type: "expense",
    amount: 50000,
    categoryName: "Makanan & Minuman",
    categoryIcon: "fork_knife",
    categoryColorHex: "#F43F5E",
    note: "Makan siang",
    date: "2026-11-16",
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  };
}

function validCategory(uid) {
  return {
    userId: uid,
    name: "Kopi Harian",
    type: "expense",
    iconKey: "coffee",
    colorHex: "#F59E0B",
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  };
}

function validRecurring(uid) {
  return {
    userId: uid,
    title: "Tagihan Listrik PLN",
    type: "expense",
    amount: 350000,
    categoryName: "Tagihan & Utilitas",
    categoryIcon: "bolt",
    categoryColorHex: "#F43F5E",
    frequency: "monthly",
    nextDueDate: "2026-11-20",
    reminderDaysBefore: 3,
    reminderHour: 9,
    reminderMinute: 0,
    enabled: true,
    note: "Token bulanan",
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  };
}

test("Unauthenticated user cannot read or write transactions, categories, or recurring", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("transactions").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("categories").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("recurringTransactions").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx1").set(validTransaction(ALICE_UID))
  );
});

test("Authenticated owner can CRUD their own transactions", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_1");
  await assertSucceeds(docRef.set(validTransaction(ALICE_UID)));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("transactions").get());
  await assertSucceeds(docRef.update({ amount: 75000, updatedAt: serverTimestamp() }));
  await assertSucceeds(docRef.delete());
});

test("Cross-user access is denied for transactions, categories, and recurring", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_1").set(validTransaction(ALICE_UID))
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_1").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("transactions").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_1").delete()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("categories").doc("cat_1").set(validCategory(BOB_UID))
  );
});

test("Shadow update with ghost field or invalid amount is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_bad1").set({
      ...validTransaction(ALICE_UID),
      isAdmin: true,
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_bad2").set({
      ...validTransaction(ALICE_UID),
      amount: -500,
    })
  );
});

test("Authenticated owner can CRUD custom categories and recurring reminders", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const catRef = aliceDb.collection("users").doc(ALICE_UID).collection("categories").doc("cat_1");
  await assertSucceeds(catRef.set(validCategory(ALICE_UID)));
  await assertSucceeds(catRef.update({ name: "Kopi Spesial", colorHex: "#10B981", updatedAt: serverTimestamp() }));
  await assertSucceeds(catRef.delete());

  const recRef = aliceDb.collection("users").doc(ALICE_UID).collection("recurringTransactions").doc("rec_1");
  await assertSucceeds(recRef.set(validRecurring(ALICE_UID)));
  await assertSucceeds(recRef.update({ reminderDaysBefore: 1, enabled: false, updatedAt: serverTimestamp() }));
  await assertSucceeds(recRef.delete());
});

test("Invalid color hex or out-of-range reminderDaysBefore is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("categories").doc("cat_bad").set({
      ...validCategory(ALICE_UID),
      colorHex: "red",
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("recurringTransactions").doc("rec_bad").set({
      ...validRecurring(ALICE_UID),
      reminderDaysBefore: 30,
    })
  );
});
