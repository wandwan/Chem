import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.TooManyListenersException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


public class Runner {
	public static void main(String[] args) {
		new Runner();
	}

	public Runner() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException ex) {
				}
				JFrame frame = new JFrame("MonomerSolver");
				ImageIcon img = new ImageIcon("Beaker.png");
				frame.setIconImage(img.getImage());
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setLayout(new BorderLayout());
				DropPane dp = new DropPane();
				frame.add(dp);
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}

	public class DropPane extends JPanel {
		boolean finished;
		List<java.io.File> files;
		private DropTarget dropTarget;
		private DropTargetHandler dropTargetHandler;
		private Point dragPoint;

		private boolean dragOver = false;
		private BufferedImage target;

		private JLabel message;

		public DropPane() {
			try {
				target = ImageIO.read(new File("target.png"));
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			setLayout(new GridBagLayout());
			message = new JLabel();
			message.setFont(message.getFont().deriveFont(Font.BOLD, 24));
			message.setText("Drag and Drop File");
			add(message);
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(400, 400);
		}

		protected DropTarget getMyDropTarget() {
			if (dropTarget == null) {
				dropTarget = new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, null);
			}
			return dropTarget;
		}

		protected DropTargetHandler getDropTargetHandler() {
			if (dropTargetHandler == null) {
				dropTargetHandler = new DropTargetHandler();
			}
			return dropTargetHandler;
		}

		@Override
		public void addNotify() {
			super.addNotify();
			try {
				getMyDropTarget().addDropTargetListener(getDropTargetHandler());
			} catch (TooManyListenersException ex) {
				ex.printStackTrace();
			}
		}

		@Override
		public void removeNotify() {
			super.removeNotify();
			getMyDropTarget().removeDropTargetListener(getDropTargetHandler());
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (dragOver) {
				Graphics2D g2d = (Graphics2D) g.create();
				g2d.setColor(new Color(0, 255, 0, 64));
				g2d.fill(new Rectangle(getWidth(), getHeight()));
				if (dragPoint != null && target != null) {
					int x = dragPoint.x - 12;
					int y = dragPoint.y - 12;
					g2d.drawImage(target, x, y, this);
				}
				g2d.dispose();
			}
		}

		protected void importFiles(final List<java.io.File> files){
			this.files = files;
			Runnable run = new Runnable() {
				@Override
				public void run() {
					message.setText("You dropped " + files.size() + " files");
					for (File f : files) {
						DataProcessor dt = new DataProcessor(f);
						try {
							dt.init();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					finished = true;
				}
			};
			SwingUtilities.invokeLater(run);
		}

		protected class DropTargetHandler implements DropTargetListener {

			protected void processDrag(DropTargetDragEvent dtde) {
				if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					dtde.acceptDrag(DnDConstants.ACTION_COPY);
				} else {
					dtde.rejectDrag();
				}
			}

			@Override
			public void dragEnter(DropTargetDragEvent dtde) {
				processDrag(dtde);
				SwingUtilities.invokeLater(new DragUpdate(true, dtde.getLocation()));
				repaint();
			}

			@Override
			public void dragOver(DropTargetDragEvent dtde) {
				processDrag(dtde);
				SwingUtilities.invokeLater(new DragUpdate(true, dtde.getLocation()));
				repaint();
			}

			@Override
			public void dropActionChanged(DropTargetDragEvent dtde) {
			}

			@Override
			public void dragExit(DropTargetEvent dte) {
				SwingUtilities.invokeLater(new DragUpdate(false, null));
				repaint();
			}

			@Override
			public void drop(DropTargetDropEvent dtde) {

				SwingUtilities.invokeLater(new DragUpdate(false, null));

				Transferable transferable = dtde.getTransferable();
				if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					dtde.acceptDrop(dtde.getDropAction());
					try {
						List<java.io.File> transferData = (List) transferable
								.getTransferData(DataFlavor.javaFileListFlavor);
						if (transferData != null && transferData.size() > 0) {
							importFiles(transferData);
							dtde.dropComplete(true);
						}

					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else {
					dtde.rejectDrop();
				}
			}
		}

		public class DragUpdate implements Runnable {

			private boolean dragOver;
			private Point dragPoint;

			public DragUpdate(boolean dragOver, Point dragPoint) {
				this.dragOver = dragOver;
				this.dragPoint = dragPoint;
			}

			@Override
			public void run() {
				DropPane.this.dragOver = dragOver;
				DropPane.this.dragPoint = dragPoint;
				DropPane.this.repaint();
			}
		}

	}
}