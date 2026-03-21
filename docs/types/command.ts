/**
 * コマンドのステータスを表す型
 */
export type CommandStatus = "stable" | "newly" | "beta" | "proposal" | "deprecated";

/**
 * コマンドの情報を表すインターフェース
 */
export interface Command {
    /**
     * コマンド名
     */
    command: string;

    /**
     * コマンドのエイリアス
     */
    aliases: string[];

    /**
     * コマンドのステータス
     */
    status: CommandStatus;
}
