package sql.client;

class SequencedRecordTypeEnumGuard{
    public static int id = 0;
}
public enum RecordType {
    Number(SequencedRecordTypeEnumGuard.id++),
    Surname(SequencedRecordTypeEnumGuard.id++),
    Salary(SequencedRecordTypeEnumGuard.id++),
    Whithheld(SequencedRecordTypeEnumGuard.id++),
    Issued(SequencedRecordTypeEnumGuard.id++),
    EnumElementsCount(SequencedRecordTypeEnumGuard.id++);

    public final int value;

    RecordType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
