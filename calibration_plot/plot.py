from sklearn.calibration import calibration_curve
from matplotlib.gridspec import GridSpec
import numpy as np
import matplotlib.pyplot as plt
import csv

dataFile = 'data/pag_sampling/pag_directed.csv'
# dataFile = 'data/pag_sampling/rfci_directed.csv'
plotLabel = 'directed (-->)'

pred_data = []
obs_data = []
with open(dataFile) as csv_file:
    csv_reader = csv.reader(csv_file, delimiter=',')
    for row in csv_reader:
        pred_data.append(float(row[1]))
        obs_data.append(int(row[2]))

y_true = np.array(obs_data)
y_pred = np.array(pred_data)

# normalize
y_prob = (y_pred - y_pred.min()) / (y_pred.max() - y_pred.min())
if y_prob.min() < 0 or y_prob.max() > 1:
    raise ValueError("y_pred has values outside [0, 1].")

prob_true, prob_pred = calibration_curve(y_true, y_prob, n_bins=10)


# Plot perfectly calibrated
plt.figure(figsize=(16, 8))
plt.plot([0, 1], [0, 1], linestyle='--')
plt.xticks(np.arange(0, 1.1, 0.1), minor=False)
plt.xticks(np.arange(0, 1, 0.02), minor=True)
plt.title('Edge Calibration')
plt.grid(visible=None)

# Plot model's calibration curve
plt.plot(prob_pred, prob_true, marker='.', label=plotLabel)

leg = plt.legend(loc='upper left')
plt.xlabel('Mean Predicted Probability')
plt.ylabel('Fraction of Positives')
plt.tight_layout()
plt.show()
